package net.chrigel.clustercode.cleanup.processor;

import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.cleanup.CleanupContext;
import net.chrigel.clustercode.cleanup.CleanupProcessor;
import net.chrigel.clustercode.scan.MediaScanSettings;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Provides a processor which deletes the source file.
 */
@XSlf4j
public class DeleteSourceProcessor implements CleanupProcessor {

    private final MediaScanSettings mediaScanSettings;

    @Inject
    DeleteSourceProcessor(MediaScanSettings mediaScanSettings) {
        this.mediaScanSettings = mediaScanSettings;
    }

    @Override
    public CleanupContext processStep(CleanupContext context) {
        log.entry(context);

        Path source = mediaScanSettings.getBaseInputDir().resolve(
                context.getTranscodeResult().getMedia().getSourcePath());

        if (!context.getTranscodeResult().isSuccessful()) {
            log.warn("Not deleting {}, since transcoding failed.", source);
            return log.exit(context);
        }

        deleteFile(source);

        return log.exit(context);
    }

    void deleteFile(Path path) {
        try {
            log.info("Deleting {}.", path);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Could not delete file {}: {}", path, e.getMessage());
        }
    }
}
