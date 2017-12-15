package net.chrigel.clustercode.cleanup.processor;

import net.chrigel.clustercode.cleanup.CleanupContext;
import net.chrigel.clustercode.cleanup.CleanupProcessor;
import net.chrigel.clustercode.cleanup.CleanupSettings;
import net.chrigel.clustercode.transcode.TranscodeResult;
import net.chrigel.clustercode.util.FileUtil;

import javax.inject.Inject;
import java.nio.file.Path;
import java.time.Clock;

/**
 * Provides a processor which moves the transcode result to a single directory.
 */
public class UnifiedOutputDirectoryProcessor
        extends AbstractOutputDirectoryProcessor
        implements CleanupProcessor {

    private final CleanupSettings cleanupSettings;

    @Inject
    UnifiedOutputDirectoryProcessor(CleanupSettings cleanupSettings,
                                    Clock clock) {
        super(clock);
        this.cleanupSettings = cleanupSettings;
        FileUtil.createDirectoriesFor(cleanupSettings.getOutputBaseDirectory());
    }

    @Override
    public CleanupContext processStep(CleanupContext context) {
        log.entry(context);
        TranscodeResult result = context.getTranscodeResult();

        if (isFailed(result)) return log.exit(context);

        Path source = result.getTemporaryPath();
        Path target = cleanupSettings.getOutputBaseDirectory().resolve(source.getFileName());

        context.setOutputPath(moveAndReplaceExisting(source, target, cleanupSettings.overwriteFiles()));
        return log.exit(context);
    }

}