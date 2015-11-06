package net.techcable.jdiff;

import lombok.*;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;

import org.apache.commons.lang3.ArrayUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileHelper {

    public static int QUEUE_SIZE = 100;

    public static void processFiles(File f, Consumer<File> action) {
        if (f.isFile()) {
            action.accept(f);
        } else {
            Queue<File> processQueue = new ArrayDeque<>(QUEUE_SIZE);
            processQueue.add(f);
            while ((f = processQueue.poll()) != null) {
                for (File child : f.listFiles()) {
                    if (child.isDirectory()) {
                        processQueue.add(child);
                    } else if (child.isFile()) {
                        action.accept(child);
                    } else throw new RuntimeException(child + "is not a file or directory");
                }
            }
        }
    }

    private static Pattern SEPARATOR_PATTERN = Pattern.compile(File.separator, Pattern.LITERAL);

    public static String getRelative(File file, File start) {
        Preconditions.checkArgument(start.isDirectory(), "%s is not a directory", start);
        final String[] startParts = SEPARATOR_PATTERN.split(start.getAbsolutePath());
        final String[] fileParts = SEPARATOR_PATTERN.split(file.getAbsolutePath());
        for (int i = 0; i < startParts.length; i++) {
            String startPart = startParts[i];
            String filePart = fileParts[i];
            Preconditions.checkNotNull(startPart, "Null startPart at %s", i);
            Preconditions.checkNotNull(filePart, "Null startPart at %s", i);
            Preconditions.checkArgument(startPart.equals(filePart), "%s is not inside %s", file, start);
        }
        String[] relativeParts = ArrayUtils.subarray(fileParts, startParts.length, fileParts.length);
        return Joiner.on(File.separatorChar).join(relativeParts);
    }

    public static final Pattern LINE_PATTERN = Pattern.compile("\\r?\\n");

    public static List<String> readLines(File file, Charset charset) throws IOException {
        return Files.readLines(file, charset);
    }
    public static void writeLines(File file, List<String> lines, Charset charset) throws IOException {
        file.createNewFile();
        @Cleanup
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new DataOutputStream(new FileOutputStream(file)), charset));// Java normalizes EOL, so we have to use insert DataOutputStream
        for (String line : lines) {
            writer.write(line);
            if (!line.endsWith("\n")) {
                writer.write('\n');
            }
        }
    }

}

