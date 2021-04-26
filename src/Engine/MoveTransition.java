package Engine;

public final class MoveTransition {

    private final TransitionStatus transitionStatus;
    private final Board transitionBoard;
    private final byte move;

    public MoveTransition(final TransitionStatus transitionStatus,
                          final Board transitionBoard,
                          final byte move){
        this.transitionStatus = transitionStatus;
        this.transitionBoard = transitionBoard;
        this.move = move;
    }

    public Board getBoard(){
        return transitionBoard;
    }

    public byte getMove() {
        return move;
    }

    public TransitionStatus getStatus() {
        return transitionStatus;
    }

}