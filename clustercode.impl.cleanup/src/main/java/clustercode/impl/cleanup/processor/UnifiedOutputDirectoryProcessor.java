package clustercode.impl.cleanup.processor;

import clustercode.api.cleanup.CleanupContext;
import clustercode.api.event.messages.TranscodeFinishedEvent;
import clustercode.impl.cleanup.CleanupConfig;
import clustercode.impl.util.FileUtil;

import javax.inject.Inject;
import java.nio.file.Path;
import java.time.Clock;

/**
 * Provides a processor which moves the transcode result to a single directory.
 */
public class UnifiedOutputDirectoryProcessor
        extends AbstractOutputDirectoryProcessor {

    private final CleanupConfig cleanupConfig;

    @Inject
    UnifiedOutputDirectoryProcessor(CleanupConfig cleanupConfig,
                                    Clock clock) {
        super(clock);
        this.cleanupConfig = cleanupConfig;
        FileUtil.createDirectoriesFor(cleanupConfig.base_output_dir());
    }

    @Override
    public CleanupContext processStep(CleanupContext context) {
        log.entry(context);
        TranscodeFinishedEvent result = context.getTranscodeFinishedEvent();

        if (isFailed(result)) return log.exit(context);

        Path source = result.getTemporaryPath();
        Path target = cleanupConfig.base_output_dir().resolve(source.getFileName());

        context.setOutputPath(moveAndReplaceExisting(source, target, cleanupConfig.overwrite_files()));
        return log.exit(context);
    }

}
