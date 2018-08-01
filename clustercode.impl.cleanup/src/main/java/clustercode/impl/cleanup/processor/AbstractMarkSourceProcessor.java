package clustercode.impl.cleanup.processor;

import clustercode.api.cleanup.CleanupContext;
import clustercode.api.cleanup.CleanupProcessor;
import clustercode.impl.cleanup.CleanupConfig;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class AbstractMarkSourceProcessor implements CleanupProcessor {

    protected final CleanupConfig cleanupConfig;
    protected XLogger log = XLoggerFactory.getXLogger(getClass());

    protected AbstractMarkSourceProcessor(CleanupConfig cleanupConfig) {
        this.cleanupConfig = cleanupConfig;
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
        return cleanupConfig.base_input_dir().resolve(
            context.getTranscodeFinishedEvent().getMedia().getSourcePath());
    }
}
