package Engine;
/**
 * Opponent
 *
 * <p>
 * This Tic-Tac-Toe opponent uses miniMax to calculate the best move.
 * Minimax is a search algorithm capable of choosing optimal moves in a zero-sum
 * game. A zero-sum game is one in which the respective gains and losses of the
 * two players (or teams) cancel each other out. This property ensures that a
 * loss for one player results in an equivalent gain for the other.
 *
 * <p>
 * The goal of minimax is to choose a move which minimizes the maximum loss
 * for the current player. The two players present in the game are designated
 * maximizer and minimizer and a sub-tree is created for each of the current
 * player's legal moves. At each level of recursion, the current player
 * alternates. at each node, all legal moves are tried out individually. When
 * the algorithm reaches a terminal node, the board is evaluated and scored,
 * and this score is returned up the tree. Each internal node selects only the
 * best score for its current player (minimum for minimizer, maximum for
 * maximizer). In this way, a single score is propagated back up each tree to
 * represent each of the initiating player's moves. Here, the highest-scoring
 * move is chosen for the initiating player. This process ensures that only
 * the wort-case scenario is considered for the initiating player-- A very
 * smart opponent.
 *
 * <p>
 * This minimax algorithm uses alpha-beta pruning to cut off large portions of
 * the search tree that need not be visited. If a part of the tree is guaranteed
 * to be cut off by a min or max node, then there is no need to look any further.
 * We can make this guarantee by setting the values of variables 'alpha' and 'beta'
 * as a score is returned from a child node up into a parent node. This process
 * relies on the fact that each search tree is navigated both iteratively and
 * recursively. For Example, after returning from a child min-node into a parent
 * max-node, alpha is set, and the algorithm enters the next child min-node. The
 * value that this min-node returns must be between alpha and beta. Beta is now
 * set during the execution of the min-node. If beta is less than alpha (since
 * this is a min-node), it will be impossible to find a value that is both greater
 * than alpha and less than beta. At this point, there is no need to continue
 * searching past this node, so we simply return beta.
 */
public final class Opponent {

    /**
     * Tuning constants.
     */
    private static final byte TERMINAL_SCORE =  10;
    private static final byte LIMIT          =  11;

    /**
     * The current game board.
     */
    private final Board board;

    /**
     * A public constructor for an Opponent.
     *
     * @param board the current game board.
     */
    public Opponent(final Board board){
        this.board = board;
    }

    /**
     * A depth-first search to score a single move.
     *
     * @param isMax whether or not the node is a max node
     * @param b     the current game board
     * @param depth the node depth
     * @param o     beta
     * @param a     alpha
     * @return      a score representing the initial move
     */
    public byte miniMax(final boolean isMax, final Board b,
                        final byte depth, byte a, byte o){
        // If computer wins, return +10 points minus depth.
        if (b.getComputer().hasWin())
            return (byte)(TERMINAL_SCORE - depth);
        // If user wins, return -10 points plus depth.
        if (b.getUser().hasWin())
            return (byte)(-TERMINAL_SCORE + depth);
        // If the state of the board is a tie...
        if (b.isFull()) return 0;

        byte score;
        if(isMax) {
            score = -LIMIT;
            for(byte i = 0; i < Board.LENGTH; i++){
                final MoveTransition mt =
                        b.getComputer().makeMove(i);
                if(mt.getStatus().isDone()){
                    score = (byte) Math.max(
                            score, miniMax(
                                    false, mt.getBoard(),
                                    (byte)(depth + 1), a, o
                            )
                    );
                    a = score;
                }
                if(a >= o) return score;
            }
        } else {
            score = LIMIT;
            for(byte i = 0; i < Board.LENGTH; i++){
                final MoveTransition mt =
                        b.getUser().makeMove(i);
                if(mt.getStatus().isDone()){
                    score = (byte) Math.min(
                            score, miniMax(
                                    true, mt.getBoard(),
                                    (byte)(depth + 1), a, o
                            )
                    );
                    o = score;
                }
                if(a >= o) return score;
            }
        }
        return score;
    }

    /**
     * A method to choose the best move for this Opponent.
     *
     * @return the best move for this Opponent
     */
    public byte chooseMove() {
        byte moveScore;
        byte bestScore = (byte)(-LIMIT);
        byte bestMove = -1;
        for(byte i = 0; i < Board.LENGTH; i++){
            final MoveTransition mt =
                    board.getComputer().makeMove(i);
            if(mt.getStatus().isDone()){
                moveScore = miniMax(
                        false, mt.getBoard(),
                        (byte)0, (byte)(-LIMIT), LIMIT
                );
                if(moveScore > bestScore) {
                    bestMove = mt.getMove();
                    bestScore = moveScore;
                }
            }
        }
        return bestMove;
    }
}

