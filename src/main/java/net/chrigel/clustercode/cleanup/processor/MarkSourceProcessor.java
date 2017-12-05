package net.chrigel.clustercode.cleanup.processor;

import net.chrigel.clustercode.cleanup.CleanupContext;
import net.chrigel.clustercode.cleanup.CleanupProcessor;
import net.chrigel.clustercode.scan.MediaScanSettings;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Provides a processor which marks the source file as done, so that it would not be rescheduled for transcoding during
 * the next scan.
 */
public class MarkSourceProcessor
    extends AbstractMarkSourceProcessor
    implements CleanupProcessor {

    @Inject
    MarkSourceProcessor(MediaScanSettings mediaScanSettings) {
        super(mediaScanSettings);
    }

    @Override
    protected CleanupContext doProcessStep(CleanupContext context) {
        Path source = getSourcePath(context);

        Path marked = source.resolveSibling(source.getFileName().toString() + mediaScanSettings.getSkipExtension());

        createMarkFile(marked, source);
        return context;
    }

    @Override
    protected boolean isStepValid(CleanupContext context) {
        Path source = getSourcePath(context);
        if (!context.getTranscodeResult().isSuccessful()) {
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
