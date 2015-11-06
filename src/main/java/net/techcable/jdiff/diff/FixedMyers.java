package net.techcable.jdiff.diff;

import java.util.List;

import com.google.common.base.Preconditions;

import difflib.ChangeDelta;
import difflib.Chunk;
import difflib.DeleteDelta;
import difflib.Delta;
import difflib.DiffAlgorithm;
import difflib.InsertDelta;
import difflib.Patch;

/**
 * A version of myers that magically works for me
 *
 * @param <T>
 */
public class FixedMyers<T> implements DiffAlgorithm<T> {
    @Override
    public Patch<T> diff(T[] first, T[] second) {
        return null;
    }

    @Override
    public Patch<T> diff(List<T> original, List<T> revised) {
        Preconditions.checkNotNull(original, "Original can't be null");
        Preconditions.checkNotNull(revised, "Revised can't be null");
        DiffPath path = buildPath(original, revised);
        return buildRevision(path, original, revised);
    }

    public static <T> DiffPath buildPath(List<T> original, List<T> revised) {
        int originalSize = original.size();
        int revisedSize = revised.size();

        int maxSize = originalSize + revisedSize + 1;
        int size = 1 + 2 * maxSize;
        int middle = size / 2;
        DiffPath[] diagonal = new DiffPath[size];

        diagonal[middle + 1] = new Snake(0, -1);
        for (int d = 0; d < maxSize; d++) {
            for (int k = -d; k <= d; k += 2) {
                int kmiddle = middle + k;
                int kplus = kmiddle + 1;
                int kminus = kmiddle - 1;
                DiffPath prev;
                int i;

                //For some reason this works, but not the other ways
                if ((k == -d) || (k != d && diagonal[kminus].i < diagonal[kplus].i)) {
                    i = diagonal[kplus].i;
                    prev = diagonal[kplus];
                } else {
                    i = diagonal[kminus].i + 1;
                    prev = diagonal[kminus];
                }

                diagonal[kminus] = null;

                int j = i - k;

                DiffPath node = new DiffPath(i, j, prev);

                /*
                 * orig and rev are zero based
                 * but the algorithm is one based
                 * that 's why there' s no +1 when indexing the sequences
                 */
                while (i < originalSize && j < revisedSize && equals(original.get(i), revised.get(j))) {
                    i++;
                    j++;
                }
                if (i > node.i) {
                    node = new Snake(i, j, node);
                }

                diagonal[kmiddle] = node;

                if (i >= originalSize && j >= revisedSize) {
                    return diagonal[kmiddle];
                }
            }

            diagonal[middle + d - 1] = null;
        }

        //This should never happen
        throw new AssertionError("couldn't find a diff path");
    }


    public static <T> Patch<T> buildRevision(DiffPath path, List<T> original, List<T> revised) {
        Patch<T> patch = new Patch<>();

        if (path.isSnake()) {
            path = path.prev;
        }
        while (path != null && path.prev != null && path.prev.j >= 0) {
            Preconditions.checkState(!path.isSnake(), "Found snake when looking for diff");

            int i = path.i;
            int j = path.j;

            path = path.prev;
            int ianchor = path.i;
            int janchor = path.j;

            Chunk<T> original_chunk = new Chunk<>(ianchor, original.subList(ianchor, i));
            Chunk<T> revised_chunk = new Chunk<>(janchor, revised.subList(janchor, j));

            Delta<T> delta;

            if (original_chunk.size() == 0 && revised_chunk.size() != 0)
                delta = new InsertDelta<>(original_chunk, revised_chunk);
            else if (original_chunk.size() > 0 && revised_chunk.size() == 0) {
                delta = new DeleteDelta<>(original_chunk, revised_chunk);
            } else {
                delta = new ChangeDelta<>(original_chunk, revised_chunk);
            }
            patch.addDelta(delta);
            if (path.isSnake()) {
                path = path.prev;
            }
        }
        return patch;
    }

    private static boolean equals(Object first, Object second) {
        return first == null ? second == null : first.hashCode() == second.hashCode() && first.equals(second);
    }

}
