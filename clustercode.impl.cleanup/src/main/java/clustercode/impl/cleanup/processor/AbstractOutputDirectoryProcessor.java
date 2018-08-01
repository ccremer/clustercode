package clustercode.impl.cleanup.processor;

import clustercode.api.cleanup.CleanupProcessor;
import clustercode.api.event.messages.TranscodeFinishedEvent;
import clustercode.impl.util.FileUtil;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public abstract class AbstractOutputDirectoryProcessor implements CleanupProcessor {

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
        log.entry(source, target, "overwrite=".concat(Boolean.toString(overwrite)));
        if (Files.exists(target) && !overwrite) {
            log.debug("Target file {} exists already. Applying timestamp to target.", target);
            target = FileUtil.getTimestampedPath(target, ZonedDateTime.now(clock), FORMATTER);
        }

        try {
            log.info("Moving file from {} to {}...", source, target);
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            return target;
        } catch (IOException e) {
            log.error("Could not move {} to {}: {}", source, target, e.toString());
            throw new RuntimeException(e);
        }
    }

    protected boolean isFailed(TranscodeFinishedEvent result) {
        if (!result.isSuccessful()) {
            log.warn("Not moving file {}, since transcoding failed.", result.getTemporaryPath().toString());
            return true;
        }
        return false;
    }
}
