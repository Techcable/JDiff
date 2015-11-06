package net.techcable.jdiff;

import lombok.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class AsyncFileWriter implements FileWriter {
    private final int jobs;
    private final BiConsumer<QueuedWrite, IOException> exceptionHandler;
    private final ExecutorService workers;

    public AsyncFileWriter(int jobs, BiConsumer<QueuedWrite, IOException> exceptionHandler) {
        this.jobs = jobs;
        this.exceptionHandler = exceptionHandler;
        this.workers = Executors.newFixedThreadPool(jobs);
    }

    public void writeLines(File file, List<String> lines, Charset charset) throws IOException {
        workers.execute(new FileWorker(new QueuedWrite(file, lines, charset)));
    }

    public void stop() {
        workers.shutdown();
        while (true) {
            try {
                if (workers.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)) return;
            } catch (InterruptedException ignored) {
            }
        }
    }

    @RequiredArgsConstructor
    @Getter
    public static class QueuedWrite {
        private final File file;
        private final List<String> lines;
        private final Charset charset;
    }

    @RequiredArgsConstructor
    private class FileWorker implements Runnable {
        private final QueuedWrite job;

        @Override
        public void run() {
            try {
                FileHelper.writeLines(job.getFile(), job.getLines(), job.getCharset());
            } catch (IOException e) {
                exceptionHandler.accept(job, e);
            }
        }
    }
}