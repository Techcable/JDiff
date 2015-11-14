package net.techcable.jdiff.cmd;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import net.techcable.jdiff.FileHelper;
import net.techcable.jdiff.PatchTask;

import difflib.PatchFailedException;

import static net.techcable.jdiff.cmd.JDiff.print;

public class PatchCommand implements Command {
    @Override
    public void execute(String[] arguments) {
        int usedArguments = 0;
        boolean quiet = false;
        for (String arg : arguments) {
            if (!arg.startsWith("-") || arg.equals("--")) break; // Signals the end of option arguments
            usedArguments++;
            switch (arg) {
                case "--quiet":
                    quiet = true;
                    break;
                default:
                    error("Unknown option", arg);
                    break;
            }
        }
        if (arguments.length - usedArguments < 3) {
            error("Not enough arguments, please see help");
        }
        File patches = new File(arguments[usedArguments]);
        File original = new File(arguments[usedArguments + 1]);
        File output = new File(arguments[usedArguments + 2]);

        if (!patches.exists()) error(patches, "doesn't exist");
        if (!original.exists()) error(original, "doesn't exist");
        if (patches.isFile()) {
            if (!original.isFile()) {
                error(original, "is not a file");
            }
            if (output.exists()) {
                error(output, "already exists");
            }
            try {
                new PatchTask(patches, original, output).run();
            } catch (IOException e) {
                error(e);
            } catch (PatchFailedException e) {
                error("Error patching file", e.getMessage());
            }
            return;
        }
        // Its a directory
        if (!original.isDirectory()) {
            error(original, "is not a directory");
        }
        if (output.exists() && !output.isDirectory()) {
            error(output, "is not a directory");
        }
        output.mkdirs();

        AtomicInteger fileCount = new AtomicInteger();
        FileHelper.processFiles(patches, (patchFile) -> {
            String relative = FileHelper.getRelative(patchFile, patches).replace(".patch", "");
            File originalFile = new File(original, relative);
            if (!originalFile.exists()) {
                error("No original found for", patchFile);
            }
            File outputFile = new File(output, relative);
            outputFile.getParentFile().mkdirs();
            if (outputFile.isDirectory()) {
                error(outputFile, "can't be a directory");
            }
            try {
                new PatchTask(patchFile, originalFile, outputFile).run();
            } catch (IOException e) {
                error(e);
            } catch (PatchFailedException e) {
                error("Error patching file "+outputFile.getPath(), e.getMessage());
            }
            fileCount.incrementAndGet();
        });
        JDiff.print("Done patching", fileCount, "files");
    }

    @Override
    public void printHelp() {
        JDiff.print("Usage: jdiff patch [options] <patches> <original> <output>");
        JDiff.print();
        JDiff.print("Applys the patches to the original files and outputs to output");
        JDiff.print("If the original is a file, the patch must be a file and the output will be a file");
        JDiff.print("The output file must not exist in file mode");
        JDiff.print();
        JDiff.print("If patches is a directory, the patches in the directory will be applied to their corresponding original");
        JDiff.print("The original file must exist");
        JDiff.print("The patched file will be output as 'name.patch' into output");
        JDiff.print("The output file must not exist");
        JDiff.print();
        JDiff.print("Options:");
        JDiff.print("  --quiet     Don't output information when a patch completes");
        JDiff.print("  --parallel  Use multiple threads to patch files");
    }
}
