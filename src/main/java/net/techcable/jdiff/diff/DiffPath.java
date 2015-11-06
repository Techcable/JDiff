package net.techcable.jdiff.diff;

import lombok.*;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
class DiffPath {
    public final int i, j; // original, revised
    public final DiffPath prev;
    private final DiffPath lastSnake;

    public DiffPath(int i, int j, DiffPath prev) {
        this.i = i;
        this.j = j;
        prev = prev.previousSnake();
        this.prev = prev;
        this.lastSnake = i < 0 || j < 0 ? null : prev.previousSnake();
    }

    public final boolean isBootstrap() {
        return i < 0 || j < 0;
    }

    public DiffPath previousSnake() {
        return lastSnake;
    }

    public boolean isSnake() {
        return false;
    }
}
