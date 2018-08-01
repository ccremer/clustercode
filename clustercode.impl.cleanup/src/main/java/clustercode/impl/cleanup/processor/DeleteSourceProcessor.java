package clustercode.impl.cleanup.processor;

import clustercode.api.cleanup.CleanupContext;
import clustercode.api.cleanup.CleanupProcessor;
import clustercode.impl.cleanup.CleanupConfig;
import lombok.extern.slf4j.XSlf4j;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Provides a processor which deletes the source file.
 */
@XSlf4j
public class DeleteSourceProcessor implements CleanupProcessor {

    private final CleanupConfig cleanupConfig;

    @Inject
    DeleteSourceProcessor(CleanupConfig cleanupConfig) {
        this.cleanupConfig = cleanupConfig;
    }

    @Override
    public CleanupContext processStep(CleanupContext context) {
        log.entry(context);

        Path source = cleanupConfig.base_input_dir().resolve(
                context.getTranscodeFinishedEvent().getMedia().getSourcePath());

        if (!context.getTranscodeFinishedEvent().isSuccessful()) {
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
