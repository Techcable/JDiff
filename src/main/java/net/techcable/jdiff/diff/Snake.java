package net.techcable.jdiff.diff;

public class Snake extends DiffPath {

    public Snake(int i, int j, DiffPath prev) {
        super(i, j, prev, null);
    }


    public Snake(int i, int j) {
        super(i, j, null, null);
    }

    public DiffPath previousSnake() {
        return this;
    }

    public boolean isSnake() {
        return true;
    }
}
