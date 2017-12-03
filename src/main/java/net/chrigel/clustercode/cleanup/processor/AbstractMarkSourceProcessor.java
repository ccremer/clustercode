package net.chrigel.clustercode.cleanup.processor;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class AbstractMarkSourceProcessor {

    protected XLogger log = XLoggerFactory.getXLogger(getClass());

    protected final void createMarkFile(Path marked, Path source) {
        try {
            log.debug("Creating file {}", marked);
            Files.createFile(marked);
        } catch (IOException e) {
            log.error("Could not create file {}: {}", marked, e.getMessage());
            log.warn("It may be possible that {} will be scheduled for transcoding again, as it could not be " +
                "marked as done.", source);
        }
    }
}
