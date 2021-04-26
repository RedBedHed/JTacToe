package GUI;

import Engine.Board;
import Engine.MoveTransition;
import Engine.Opponent;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class Game extends Observable {

    private static final Dimension GAME_DIMENSION;
    private static final Dimension TILE_PANEL_DIMENSION;
    private static final int TILE_IMAGE_WIDTH;
    private static final String IMAGES_PATH;
    private static final int BORDER_THICKNESS;
    private static final int ROW_COL_NUMBER;
    static {
        GAME_DIMENSION = new Dimension(1024, 1024);
        IMAGES_PATH = "C:\\Users\\evcmo\\IdeaProjects\\Tic-Tac-Toe\\art\\";
        TILE_PANEL_DIMENSION = new Dimension(341, 341);
        BORDER_THICKNESS = 10;
        TILE_IMAGE_WIDTH = 300;
        ROW_COL_NUMBER = 3;
    }

    private JFrame gameFrame;
    private GamePanel gamePanel;

    private Game() {
        gameFrame = new JFrame("JTicTac");
        try { gameFrame.setIconImage(ImageIO.read(
                new File(IMAGES_PATH + "o.gif")
        )); } catch(IOException e){ e.printStackTrace(); }
        gameFrame.setSize(GAME_DIMENSION);
        gameFrame.setResizable(false);
        gameFrame.setLayout(new BorderLayout());
        gamePanel = new GamePanel();
        gameFrame.add(gamePanel, BorderLayout.CENTER);
        addObserver(new GameObserver());
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setVisible(true);
    }

    public static void main(String[] args) { new Game(); }

    private final synchronized void reset() {
        gamePanel.reset();
        SwingUtilities.invokeLater(() -> {
            gameFrame.getContentPane().revalidate();
            gameFrame.getContentPane().repaint();
        });
        Set<Thread> threads = Thread.getAllStackTraces().keySet();
        for (final Thread t : threads) {
            String name = t.getName();
            Thread.State state = t.getState();
            int priority = t.getPriority();
            String type = t.isDaemon() ? "Daemon" : "Normal";
            System.out.printf("%-20s \t %s \t %d \t %s\n", name, state, priority, type);
        }
        System.out.println("\n\n\n\n");
    }

    private enum GameStatus {
        RUNNING {
            @Override
            public boolean isGameOver() {
                return false;
            }
        },
        GAME_OVER {
            @Override
            public boolean isGameOver() {
                return true;
            }
        };
        public abstract boolean isGameOver();
    }

    private enum PlayerType {
        USER {
            @Override
            public boolean isUser() {
                return true;
            }
        },
        COMPUTER {
            @Override
            public boolean isUser() {
                return false;
            }
        };
        public abstract boolean isUser();
    }

    private final class GameObserver implements Observer {

        private GameObserver(){}

        @Override
        public void update(final Observable o, final Object arg) {
            if(arg == null)
                throw new NullPointerException();
            if(!(arg instanceof PlayerType))
                throw new IllegalArgumentException();
            if(((PlayerType) arg).isUser()) {
                if (!gamePanel.board.getUser().hasWin()
                        && !gamePanel.board.getComputer().hasWin()) {
                    new OpponentWorker().execute();
                }
            }
        }

    }

    private final class OpponentWorker extends SwingWorker<Byte, Object> {

        private static final int SLEEP_TIME = 500;

        private OpponentWorker(){}

        @Override
        protected Byte doInBackground() throws Exception {
            return new Opponent(gamePanel.board).chooseMove();
        }

        @Override
        public void done(){
            try {
                final MoveTransition mt1 =
                        gamePanel.board.getComputer().makeMove(get());
                if (!gamePanel.board.isFull()) {
                    try { TimeUnit.MILLISECONDS.sleep(SLEEP_TIME); }
                    catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                if (mt1.getStatus().isDone()) {
                    gamePanel.board = mt1.getBoard();
                    if (!gamePanel.status.isGameOver()
                            && gamePanel.board.getComputer().hasWin()) {
                        gamePanel.status = GameStatus.GAME_OVER;
                        SwingUtilities.invokeLater(() -> gamePanel.draw());
                        congratulateWinner("You Lose!");
                    } else {
                        SwingUtilities.invokeLater(() -> gamePanel.draw());
                        notifyWatcher(PlayerType.COMPUTER);
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void notifyWatcher(final PlayerType type) {
        setChanged();
        notifyObservers(type);
    }

    private void congratulateWinner(final String s){
        final int yesNoOption = JOptionPane.showConfirmDialog(
                gameFrame, s + " Play Again?"
        );
        if (yesNoOption == JOptionPane.YES_OPTION) {
            reset();
        } else {
            System.exit(0);
        }
    }

    private final class GamePanel extends JPanel {

        private Board board;
        private GameStatus status;
        private List<TilePanel> tiles;

        private GamePanel() {
            super(new GridLayout(ROW_COL_NUMBER,ROW_COL_NUMBER));
            setSize(GAME_DIMENSION);
            setBackground(Color.BLACK);
            setBorder(BorderFactory.createLineBorder(
                    Color.BLACK, BORDER_THICKNESS
            ));
            init();
        }

        private synchronized void init() {
            board = Board.makeDefault();
            status = GameStatus.RUNNING;
            final ArrayList<TilePanel> al = new ArrayList<>();
            for(byte i = 0; i < Board.LENGTH; i++) {
                final TilePanel tp = new TilePanel(i);
                al.add(tp);
                add(tp);
            }
            tiles = al;
            validate();
        }

        private void draw() {
            removeAll();
            for(TilePanel t: tiles) {
                t.draw();
                add(t);
            }
            validate();
            repaint();
        }

        private void reset() {
            init();
            draw();
        }

        private class TilePanel extends JPanel {

            private final byte cell;

            private TilePanel(final byte cell) {
                super(new GridBagLayout());
                setPreferredSize(TILE_PANEL_DIMENSION);
                setBorder(BorderFactory.createLineBorder(Color.BLACK, BORDER_THICKNESS));
                setBackground(Color.BLACK);
                this.cell = cell;
                assignIcon();
                addMouseListener(new MouseAdapter(){
                    @Override
                    public void mouseClicked(MouseEvent e){
                        final MoveTransition mt0 = board.getUser().makeMove(cell);
                        if(mt0.getStatus().isDone()
                                && !mt0.getBoard().getUser().hasWin()
                                && !mt0.getBoard().getComputer().hasWin()) {
                            board = mt0.getBoard();
                            SwingUtilities.invokeLater(() -> {
                                gamePanel.draw();
                                if(!status.isGameOver()
                                        && board.getUser().hasWin()) {
                                    status = GameStatus.GAME_OVER;
                                    congratulateWinner("You Win!");
                                } else if(!status.isGameOver() && board.isFull()) {
                                    status = GameStatus.GAME_OVER;
                                    congratulateWinner("It's a Tie!");
                                } else {
                                    notifyWatcher(PlayerType.USER);
                                }
                            });
                        }
                    }
                });
            }

            private void draw() {
                removeAll();
                setBackground(Color.BLACK);
                assignIcon();
                validate();
                repaint();
            }

            private void assignIcon() {
                try {
                    final BufferedImage buffy = ImageIO.read(new File(
                            IMAGES_PATH + (
                                    board.getSquare(cell)
                            ) + ".gif"
                    ));
                    add(new JLabel(new ImageIcon(
                            buffy.getScaledInstance(
                                    TILE_IMAGE_WIDTH, TILE_IMAGE_WIDTH,
                                    Image.SCALE_SMOOTH
                            )
                    )));
                } catch(IOException e){ e.printStackTrace(); }
            }

        }
    }

}

