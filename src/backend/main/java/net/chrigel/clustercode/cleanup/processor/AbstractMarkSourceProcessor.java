package net.chrigel.clustercode.cleanup.processor;

import net.chrigel.clustercode.cleanup.CleanupContext;
import net.chrigel.clustercode.cleanup.CleanupProcessor;
import net.chrigel.clustercode.scan.MediaScanSettings;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class AbstractMarkSourceProcessor implements CleanupProcessor {

    protected final MediaScanSettings mediaScanSettings;
    protected XLogger log = XLoggerFactory.getXLogger(getClass());

    protected AbstractMarkSourceProcessor(MediaScanSettings mediaScanSettings) {
        this.mediaScanSettings = mediaScanSettings;
    }

    @Override
    public final CleanupContext processStep(CleanupContext context) {
        log.entry(context);
        if (!isStepValid(context)) return log.exit(context);
        return log.exit(doProcessStep(context));
    }

    protected abstract CleanupContext doProcessStep(CleanupContext context);

    protected abstract boolean isStepValid(CleanupContext cleanupContext);

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

    protected Path getSourcePath(CleanupContext context) {
        return mediaScanSettings.getBaseInputDir().resolve(
            context.getTranscodeResult().getMedia().getSourcePath());
    }
}
