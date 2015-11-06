package net.techcable.jdiff.cmd;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import net.techcable.jdiff.AsyncFileWriter;
import net.techcable.jdiff.FileHelper;
import net.techcable.jdiff.DiffTask;
import net.techcable.jdiff.FileWriter;

import static net.techcable.jdiff.cmd.JDiff.print;

public class DiffCommand implements Command {
    @Override
    public void execute(String[] arguments) {
        int usedArguments = 0;
        boolean quiet = false;
        boolean parallel = false;
        for (String arg : arguments) {
            if (!arg.startsWith("-") || arg.equals("--")) break; // Signals the end of option arguments
            usedArguments++;
            switch (arg) {
                case "--quiet":
                    quiet = true;
                    break;
                case "--parallel":
                    parallel = true;
                    break;
                default:
                    error("Unknown option", arg);
                    break;
            }
        }
        if (arguments.length - usedArguments < 3) {
            error("Not enough arguments, please see help");
        }
        FileWriter fileWriter = FileHelper::writeLines;
        File original = new File(arguments[usedArguments]);
        File modified = new File(arguments[usedArguments + 1]);
        File output = new File(arguments[usedArguments + 2]);
        if (!original.exists()) error(original, "doesn't exist");
        if (!modified.exists()) error(modified, "doesn't exist");
        if (original.isFile()) {
            if (!modified.isFile()) {
                error(modified, "is not a file");
            }
            if (output.exists()) {
                error(output, "already exists");
            }
            try {
                new DiffTask(fileWriter, original, modified, output).run();
            } catch (IOException e) {
                error(e);
            }
            return;
        }
        // Its a directory
        if (!modified.isDirectory()) {
            error(modified, "is not a directory");
        }
        if (output.exists() && !output.isDirectory()) {
            error(output, "is not a directory");
        }
        output.mkdirs();
        AsyncFileWriter asyncFileWriter = parallel ? new AsyncFileWriter(3, (job, e) -> error(e)) : null;
        AtomicInteger fileCount = new AtomicInteger();
        final boolean finalQuiet = quiet;
        FileHelper.processFiles(modified, (modifiedFile) -> {
            String relative = FileHelper.getRelative(modifiedFile, modified);
            File originalFile = new File(original, relative);
            if (!originalFile.exists()) {
                if (!finalQuiet) print("No original found for", relative);
                return;
            }
            File outputFile = new File(output, relative + ".patch");
            outputFile.getParentFile().mkdirs();
            if (outputFile.isDirectory()) {
                error(outputFile, "can't be a directory");
            }
            fileCount.incrementAndGet();
            if (!finalQuiet) print("Diffing", modifiedFile);
            try {
                new DiffTask(asyncFileWriter == null ? fileWriter : asyncFileWriter, originalFile, modifiedFile, outputFile).run();
            } catch (IOException e) {
                error(e);
            }
        });
        if (asyncFileWriter != null) {
            asyncFileWriter.stop();
        }
        print("Done diffing", fileCount, "files");
    }

    @Override
    public void printHelp() {
        print("Usage: jdiff diff [options] <original> <modified> <output>");
        print();
        print("Outputs changes between the original file/dir and the modified file/dir to the output in unified diff format");
        print("If the original is a file, the modified must be a file and the output will be a file");
        print("The output file must not exist in file mode");
        print();
        print("If the original is a directory, files which exist in both the modified and the original will be compared");
        print("The diff of the file will be output as 'name.patch' into output");
        print("The output will be overridden if it exists");
        print();
        print("Options:");
        print("  --quiet     Don't output information when a diff completes");
        print("  --parallel  Use multiple threads to diff files");
    }
}
