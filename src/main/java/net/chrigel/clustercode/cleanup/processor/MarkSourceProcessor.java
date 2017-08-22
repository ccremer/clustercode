package net.chrigel.clustercode.cleanup.processor;

import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.scan.MediaScanSettings;
import net.chrigel.clustercode.cleanup.CleanupContext;
import net.chrigel.clustercode.cleanup.CleanupProcessor;
import net.chrigel.clustercode.util.LogUtil;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Provides a processor which marks the source file as done, so that it would not be rescheduled for transcoding during
 * the next scan.
 */
@XSlf4j
public class MarkSourceProcessor implements CleanupProcessor {

    private final MediaScanSettings mediaScanSettings;

    @Inject
    MarkSourceProcessor(MediaScanSettings mediaScanSettings) {
        this.mediaScanSettings = mediaScanSettings;
    }

    @Override
    public CleanupContext processStep(CleanupContext context) {
        log.entry(context);
        Path source = mediaScanSettings.getBaseInputDir().resolve(
                context.getTranscodeResult().getMedia().getSourcePath());
        Path marked = source.resolveSibling(source.getFileName().toString() + mediaScanSettings.getSkipExtension());

        if (!context.getTranscodeResult().isSuccessful()) {
            log.warn("Not marking {} as done, since transcoding failed.", source);
            log.exit(context);
        }

        if (!Files.exists(source)) return LogUtil.logWarnAndExit(context, log,
                "Not marking {} as done, since the file does not exist (anymore).", source);

        try {
            log.debug("Creating file {}", marked);
            Files.createFile(marked);
        } catch (IOException e) {
            log.error("Could not create file {}: {}", marked, e.getMessage());
            log.warn("It may be possible that {} will be scheduled for transcoding again, as it could not be " +
                    "marked as done.", source);
        }
        return log.exit(context);
    }

}
