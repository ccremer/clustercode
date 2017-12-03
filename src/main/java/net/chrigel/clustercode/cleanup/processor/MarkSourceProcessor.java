package net.chrigel.clustercode.cleanup.processor;

import net.chrigel.clustercode.cleanup.CleanupContext;
import net.chrigel.clustercode.cleanup.CleanupProcessor;
import net.chrigel.clustercode.scan.MediaScanSettings;
import net.chrigel.clustercode.util.LogUtil;

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
            return log.exit(context);
        }

        if (!Files.exists(source)) return LogUtil.logWarnAndExit(context, log,
            "Not marking {} as done, since the file does not exist (anymore).", source);

        createMarkFile(marked, source);
        return log.exit(context);
    }

}
