package Engine;

import java.util.Scanner;

public final class TextTacToe {
    public static final String MENU;
    public static final Scanner stdin;
    static {
        MENU = "\nMenu:\n0 1 2\n3 4 5\n6 7 8\n(Enter any other integer to quit)\n\n";
        stdin = new Scanner(System.in);
    }
    public static void main(String[] args){
        do {
            Board b = Board.makeDefault();
            while (!b.isFull()
                    && !b.getUser().hasWin()
                    && !b.getComputer().hasWin()) {
                System.out.print(MENU);
                b = makeUserMove(b);
                long start = System.nanoTime();
                b = b.getComputer().makeMove(
                        new Opponent(b).chooseMove()
                ).getBoard();
                System.out.println(System.nanoTime() - start);
                System.out.println("\nBoard:");
                System.out.println(b);
            }
            if (b.getUser().hasWin())
                System.out.println("You win!\n");
            else if (b.getComputer().hasWin())
                System.out.println("You Lose!\n");
            else System.out.println("Tie!\n");
            stdin.nextLine();
        } while('y' == getChar("Play again? (y/n)>> "));
    }
    public static char getChar(final String message) {
        System.out.print(message);
        final char x = stdin.nextLine().trim().charAt(0);
        if (x != 'n' && x != 'y') return getChar(message);
        return x;
    }
    public static Board makeUserMove(final Board b) {
        return tryUserMove(b.getUser().makeMove(getByte()));
    }
    public static Board tryUserMove(final MoveTransition mt){
        if(mt.getMove() == -1) giveQuitOption();
        if(mt.getStatus().isDone()) return mt.getBoard();
        return makeUserMove(mt.getBoard());
    }
    public static byte getByte(){
        System.out.print("Move (integer)>> ");
        if(!stdin.hasNextByte()) { stdin.next(); return getByte(); }
        return stdin.nextByte();
    }
    public static void giveQuitOption() {
        stdin.nextLine();
        if('y' == getChar("Quit? (y/n)>> ")) System.exit(0);
    }
}
