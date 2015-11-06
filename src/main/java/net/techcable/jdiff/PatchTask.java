package net.techcable.jdiff;

import lombok.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import difflib.DiffUtils;
import difflib.Patch;
import difflib.PatchFailedException;

import static com.google.common.base.Charsets.UTF_8;

@RequiredArgsConstructor
public class PatchTask {
    private final File patchFile, originalFile, outputFile;

    public void run() throws IOException, PatchFailedException {
        List<String> patchLines = FileHelper.readLines(patchFile, UTF_8);
        Patch<String> patch = DiffUtils.parseUnifiedDiff(patchLines);
        List<String> originalLines = FileHelper.readLines(originalFile, UTF_8);
        List<String> modifiedLines = DiffUtils.patch(originalLines, patch);
        if (Thread.interrupted()) return;
        FileHelper.writeLines(outputFile, modifiedLines, UTF_8);
    }
}
