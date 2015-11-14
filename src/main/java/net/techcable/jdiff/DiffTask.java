package net.techcable.jdiff;

import lombok.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import net.techcable.jdiff.diff.FixedMyers;

import difflib.DiffUtils;
import difflib.Patch;

import static com.google.common.base.Charsets.UTF_8;

@RequiredArgsConstructor
public class DiffTask {
    private final FileWriter fileWriter;
    private final File originalFile, modifiedFile, outputFile;

    public static final int CONTEXT_SIZE = 5;

    public void run() throws IOException {
        List<String> originalLines = FileHelper.readLines(originalFile, UTF_8);
        List<String> modifiedLines = FileHelper.readLines(modifiedFile, UTF_8);
        Patch<String> diff = DiffUtils.diff(originalLines, modifiedLines, new FixedMyers<>());
        if (diff.getDeltas().isEmpty()) return;
        List<String> diffLines = DiffUtils.generateUnifiedDiff(FileHelper.getNormalizedPath(originalFile), FileHelper.getNormalizedPath(modifiedFile), originalLines, diff, CONTEXT_SIZE);
        if (Thread.interrupted()) return;
        fileWriter.writeLines(outputFile, diffLines, UTF_8);
    }
}
