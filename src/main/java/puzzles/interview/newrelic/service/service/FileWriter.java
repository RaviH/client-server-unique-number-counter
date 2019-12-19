package puzzles.interview.newrelic.service.service;

import io.micronaut.context.annotation.Context;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.scheduling.annotation.Async;
import lombok.extern.slf4j.Slf4j;
import puzzles.interview.newrelic.service.event.WriteToFileEvent;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Writes to file upon receiving {@link WriteToFileEvent} event
 */
@Context
@Slf4j
public class FileWriter {

    private final File outputFile;
    private final FileOutputStream outputStream;

    public FileWriter() throws IOException {

        this.outputFile = createNewFile();
        this.outputStream = new FileOutputStream(outputFile);
    }

    private File createNewFile() throws IOException {

        final File outputFile = new File("/tmp/numbers.log");
        // Delete the file if it already exists, extra precaution.
        if (outputFile.exists()) {
            outputFile.delete();
        }
        outputFile.createNewFile();
        outputFile.deleteOnExit();
        return outputFile;
    }

    /**
     * Write number to file.
     *
     * @param writeToFileEvent event has number data in it.
     * @throws IOException
     */
    @EventListener
    @Async
    public void writeToFile(WriteToFileEvent writeToFileEvent) throws IOException {

        outputStream.write(writeToFileEvent.getNumber().getBytes());
    }

    /**
     * Close output stream before closing.
     *
     * @throws IOException
     */
    @PreDestroy
    public void cleanup() throws IOException {

        if (outputStream != null) {

            outputStream.close();
        }
    }
}
