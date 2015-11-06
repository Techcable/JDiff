package net.techcable.jdiff;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

public interface FileWriter {

    public void writeLines(File file, List<String> lines, Charset charset) throws IOException;
}
