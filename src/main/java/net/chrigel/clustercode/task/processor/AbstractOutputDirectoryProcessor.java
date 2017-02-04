package net.chrigel.clustercode.task.processor;

import net.chrigel.clustercode.util.FileUtil;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public abstract class AbstractOutputDirectoryProcessor {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd.HH-mm-ss");
    protected final XLogger log = XLoggerFactory.getXLogger(getClass());
    protected final Clock clock;

    protected AbstractOutputDirectoryProcessor(Clock clock) {
        this.clock = clock;
    }

    /**
     * Moves the source file to the target file. If the file exists and {@code overwrite} is enabled, then the file is
     * being overwritten. If the file exists and {@code overwrite} is disabled, then the target path is being modified
     * using {@link FileUtil#getTimestampedPath(Path, TemporalAccessor, DateTimeFormatter)}.
     *
     * @param source    the source file.
     * @param target    the target outputfile.
     * @param overwrite the overwrite flag.
     * @return the final target path.
     * @throws RuntimeException if the file could not be moved.
     */
    protected Path moveAndReplaceExisting(Path source, Path target, boolean overwrite) {
        if (Files.exists(target) && !overwrite) {
            target = FileUtil.getTimestampedPath(target, ZonedDateTime.now(clock), FORMATTER);
        }

        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            return target;
        } catch (IOException e) {
            log.error("Could not move {} to {}: {}", source, target, e.toString());
            log.info("Aborting cleanup. Please do it manually.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates the parent directories for the given path.
     *
     * @param target the target path. Must not be a root path.
     * @throws RuntimeException if the dirs could not be created.
     * @see Path#getParent()
     */
    protected void createParentDirectoriesFor(Path target) {
        createDirectoriesFor(target.getParent());
    }

    /**
     * Creates the parent directories for the given path.
     *
     * @param target the target path.
     * @throws RuntimeException if the dirs could not be created.
     * @see Files#createDirectories(Path, FileAttribute[])
     */
    protected void createDirectoriesFor(Path target) {
        try {
            Files.createDirectories(target);
        } catch (IOException e) {
            log.error("Could not create parent directories for {}: {}", target, e);
            throw new RuntimeException(e);
        }
    }
}