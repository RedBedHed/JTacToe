package Engine;

import java.util.List;

public final class Board {
    /**
     * The length of this Board.
     */
    public static final short LENGTH = 9;

    /**
     * These bit-masks correspond to to each square on
     * the tic-tac-toe board. On the bit-board, empty
     * squares are represented by zero and occupied
     * squares are represented by one. Placing an
     * insignia on an empty square is accomplished by
     * OR-ing the bit-board with the appropriate mask.
     */
    public static final short ZERO  = 0x0100;
    public static final short ONE   = 0x0080;
    public static final short TWO   = 0x0040;
    public static final short THREE = 0x0020;
    public static final short FOUR  = 0x0010;
    public static final short FIVE  = 0x0008;
    public static final short SIX   = 0x0004;
    public static final short SEVEN = 0x0002;
    public static final short EIGHT = 0x0001;

    /**
     * Table for quick conversions.
     */
    public static final List<Short> INT_TO_MOVE;
    static {
        INT_TO_MOVE = List.of(
                ZERO, ONE, TWO, THREE, FOUR, FIVE,
                SIX, SEVEN, EIGHT
        );
    }

    /**
     * These bit-masks represent the various end-game
     * positions in tic-tac-toe. Checking for one of
     * these positions is accomplished by AND-ing the
     * appropriate bit-board with the appropriate bit
     * -mask and then checking for equality against the
     * same bit-mask.
     */
    public static final short ROW_ZERO_MASK   = 0x01C0;
    public static final short ROW_ONE_MASK    = 0x0038;
    public static final short ROW_TWO_MASK    = 0x0007;
    public static final short COL_ZERO_MASK   = 0x0124;
    public static final short COL_ONE_MASK    = 0x0092;
    public static final short COL_TWO_MASK    = 0x0049;
    public static final short RL_DIAG_MASK    = 0x0054;
    public static final short LR_DIAG_MASK    = 0x0111;
    public static final short FULL_BOARD_MASK = 0x01FF;

    /**
     * A bit-board to represent all occupied squares.
     */
    private final short allSquares;

    /**
     * Whether or not this Board is full.
     */
    private final boolean isFull;

    /**
     * The current user player.
     */
    private final Player user;

    /**
     * The current computer player.
     */
    private final Player computer;

    /**
     * A byte to represent the nil insignia in ASCII.
     */
    private final byte nilInsignia;

    /**
     * A private constructor for a Board (for Builder use).
     *
     * @param b the Builder to use in instantiating this Board
     */
    private Board(final Builder b) {
        this.allSquares = (short)(b.userSquares | b.computerSquares);
        this.isFull = (allSquares & FULL_BOARD_MASK) == FULL_BOARD_MASK;
        this.nilInsignia = b.nilInsignia;
        this.user = new Player(
                this, b.userSquares, b.userInsignia
        ) {
            @Override
            protected Board updateBoard(short move) {
                return new Builder()
                        .setNilInsignia(nilInsignia)
                        .setUserSquares((short)(user.squares | move))
                        .setComputerSquares(computer.squares)
                        .setUserInsignia(user.insignia)
                        .setComputerInsignia(computer.insignia)
                        .build();
            }
        };
        this.computer = new Player(
                this, b.computerSquares, b.computerInsignia
        ) {
            @Override
            protected Board updateBoard(short move) {
                return new Builder()
                        .setNilInsignia(nilInsignia)
                        .setUserSquares(user.squares)
                        .setComputerSquares((short)(computer.squares | move))
                        .setUserInsignia(user.insignia)
                        .setComputerInsignia(computer.insignia)
                        .build();
            }
        };
    }

    /**
     * A static factory to build a default Board with
     * empty bit-boards, a user player, and a computer
     * player.
     *
     * @return a default Board
     */
    public static Board makeDefault() {
        return new Builder().build();
    }

    /**
     * A Builder for a Board that gives the client the
     * option to set the user occupied squares, the computer
     * occupied squares, the insignias of both players,
     * or the nil insignia.
     */
    public static final class Builder {
        private short userSquares;
        private short computerSquares;
        private byte userInsignia;
        private byte computerInsignia;
        private byte nilInsignia;

        public Builder() {
            userSquares = 0;
            computerSquares = 0;
            userInsignia = 'x';
            computerInsignia = 'o';
            nilInsignia = '-';
        }

        public Builder setUserSquares(final short squares) {
            userSquares = squares;
            return this;
        }

        public Builder setComputerSquares(final short squares) {
            computerSquares = squares;
            return this;
        }

        public Builder setUserInsignia(final byte insignia) {
            userInsignia = insignia;
            return this;
        }

        public Builder setComputerInsignia(final byte insignia) {
            computerInsignia = insignia;
            return this;
        }

        public Builder setNilInsignia(final byte insignia) {
            nilInsignia = insignia;
            return this;
        }

        public Board build() {
            return new Board(this);
        }
    }

    /**
     * A method to expose the user.
     *
     * @return the user player.
     */
    public Player getUser() {
        return user;
    }

    /**
     * A method to expose the computer.
     *
     * @return the computer player.
     */
    public Player getComputer() {
        return computer;
    }

    /**
     * A method to determine whether or not this board is full.
     *
     * @return whether or not this board is full.
     */
    public boolean isFull() {
        return isFull;
    }

    /**
     * A method to get the insignia at the given squareId.
     * In case of a win-- if this square belongs to the winning
     * sequence of squares-- the insignia returned will be
     * the character above the insignia of the winning player
     * in Unicode.
     *
     * @param squareId the ID of the square to get
     * @return the insignia at the specified square
     */
    public char getSquare(final short squareId) {
        if(squareId >= 0 && squareId < LENGTH) {
            final short squareMask = INT_TO_MOVE.get(squareId);
            return (char) (
                    user.ownsHighlightSquare(squareMask)               ?
                        user.insignia + 1                              :
                    user.ownsSquare(squareMask)                        ?
                        user.insignia                                  :
                    computer.ownsHighlightSquare(squareMask)           ?
                        computer.insignia + 1                          :
                    computer.ownsSquare(squareMask)                    ?
                        computer.insignia                              :
                            nilInsignia
            );
        }
        return (char) nilInsignia;
    }

    /**
     * A class to represent a player on a Board.
     * This class may not be instantiated.
     * This class may not be subclassed from outside
     * of the Board scope as its constructor is private.
     */
    public static abstract class Player {
        private final Board board;
        private final short squares;
        private final byte insignia;
        private final boolean hasWin;
        private final short highlightSquares;

        /**
         * A private constructor for a Player.
         */
        private Player(final Board board,
                       final short playerSquares,
                       final byte insignia) {
            this.board = board;
            this.squares = playerSquares;
            this.insignia = insignia;
            this.highlightSquares =
                    ((playerSquares & ROW_ZERO_MASK) == ROW_ZERO_MASK) ?
                         ROW_ZERO_MASK                                 :
                    ((playerSquares & ROW_ONE_MASK)  == ROW_ONE_MASK)  ?
                         ROW_ONE_MASK                                  :
                    ((playerSquares & ROW_TWO_MASK)  == ROW_TWO_MASK)  ?
                         ROW_TWO_MASK                                  :
                    ((playerSquares & COL_ZERO_MASK) == COL_ZERO_MASK) ?
                         COL_ZERO_MASK                                 :
                    ((playerSquares & COL_ONE_MASK)  == COL_ONE_MASK)  ?
                         COL_ONE_MASK                                  :
                    ((playerSquares & COL_TWO_MASK)  == COL_TWO_MASK)  ?
                         COL_TWO_MASK                                  :
                    ((playerSquares & LR_DIAG_MASK)  == LR_DIAG_MASK)  ?
                         LR_DIAG_MASK                                  :
                    ((playerSquares & RL_DIAG_MASK)  == RL_DIAG_MASK)  ?
                         RL_DIAG_MASK                                  :
                            0;
            this.hasWin = highlightSquares != 0;
        }

        /**
         * A method to indicate whether or not this player
         * has a win.
         *
         * @return whether or not this player has a win
         */
        public boolean hasWin(){
            return hasWin;
        }

        /**
         * A method to make a move for this player,
         * returning a move transition struct
         * that packages the status of the current
         * move along with the updated board and
         * the current move id.
         *
         * @param moveId the current move ID
         * @return a MoveTransition
         */
        public MoveTransition makeMove(final byte moveId) {
            if(moveId >= 0 && moveId < LENGTH) {
                final short move = INT_TO_MOVE.get(moveId);
                if((board.allSquares & move) == 0) {
                    return new MoveTransition(
                            TransitionStatus.DONE,
                            updateBoard(move), moveId
                    );
                }
            }
            return new MoveTransition(
                    TransitionStatus.ILLEGAL, board, (byte)(-1)
            );
        }

        /**
         * A private method to indicate whether or not this player
         * owns the specified square.
         *
         * @param squareMask the mask corresponding to the squareId
         * @return whether or not this Player owns the specified square
         */
        private boolean ownsSquare(final short squareMask) {
            return (squareMask & squares) == squareMask;
        }

        /**
         * A private method to indicate whether or not this player
         * owns the specified square on the hi-light squares
         * bit-board.
         *
         * @param squareMask the mask corresponding to the squareId
         * @return whether or not this Player owns the specified highlight square
         */
        private boolean ownsHighlightSquare(final short squareMask) {
            return hasWin && (squareMask & highlightSquares) == squareMask;
        }

        /**
         * A method to update the board for the this Player.
         *
         * @param move the move mask to use
         * @return an updated Board
         */
        protected abstract Board updateBoard(short move);
    }

    /** @inheritDoc */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        short u = user.squares, c = computer.squares;
        for(short i = 0; i < LENGTH; i += 3) {
            for(short j = i; j < i + 3; j++) {
                sb.insert(0,
                        (u & 1) == 1 ? (char) user.insignia     :
                        (c & 1) == 1 ? (char) computer.insignia :
                                       (char) nilInsignia
                ).insert(0," ");
                u >>>= 1; c >>>= 1;
            }
            sb.insert(0,"\n");
        }
        return sb.toString();
    }
}
