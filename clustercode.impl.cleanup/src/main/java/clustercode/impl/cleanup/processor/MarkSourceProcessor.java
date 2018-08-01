package clustercode.impl.cleanup.processor;

import clustercode.api.cleanup.CleanupContext;
import clustercode.impl.cleanup.CleanupConfig;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Provides a processor which marks the source file as done, so that it would not be rescheduled for transcoding during
 * the next scan.
 */
public class MarkSourceProcessor
    extends AbstractMarkSourceProcessor {

    @Inject
    MarkSourceProcessor(CleanupConfig cleanupConfig) {
        super(cleanupConfig);
    }

    @Override
    protected CleanupContext doProcessStep(CleanupContext context) {
        Path source = getSourcePath(context);

        Path marked = source.resolveSibling(source.getFileName().toString() + cleanupConfig.skip_extension());

        createMarkFile(marked, source);
        return context;
    }

    @Override
    protected boolean isStepValid(CleanupContext context) {
        Path source = getSourcePath(context);
        if (!context.getTranscodeFinishedEvent().isSuccessful()) {
            log.warn("Not marking {} as done, since transcoding failed.", source);
            return false;
        }
        if (!Files.exists(source)) {
            log.warn("Not marking {} as done, since the file does not exist (anymore).", source);
            return false;
        }
        return true;
    }

}
