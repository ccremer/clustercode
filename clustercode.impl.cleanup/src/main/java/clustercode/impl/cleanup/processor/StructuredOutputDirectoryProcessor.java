package clustercode.impl.cleanup.processor;

import clustercode.api.cleanup.CleanupContext;
import clustercode.api.event.messages.TranscodeFinishedEvent;
import clustercode.impl.cleanup.CleanupConfig;
import clustercode.impl.util.FileUtil;

import javax.inject.Inject;
import java.nio.file.Path;
import java.time.Clock;

/**
 * Provides a processor which recreates the source directory tree in the configured root output directory. The priority
 * directory will be omitted. If overwriting is disabled, the file will have a timestamp appended in the file name.
 */
public class StructuredOutputDirectoryProcessor
        extends AbstractOutputDirectoryProcessor {

    private final CleanupConfig cleanupConfig;

    @Inject
    StructuredOutputDirectoryProcessor(CleanupConfig cleanupConfig,
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

        Path media = result.getMedia().getSourcePath();

        Path target = createOutputDirectoryTree(media);

        Path tempFile = source.getFileName();
        Path finalPath = target.getParent().resolve(tempFile);
        FileUtil.createParentDirectoriesFor(finalPath);
        context.setOutputPath(moveAndReplaceExisting(source, finalPath, cleanupConfig.overwrite_files()));
        return log.exit(context);
    }

    /**
     * Creates the directory tree in {@link CleanupConfig#base_output_dir()} using the given path. The given
     * path will be stripped from the root directory. The parent directories of the target file will be created if the
     * tree does not exist.
     * <p>
     * Example: {@code mediaSource} is "0/subdir/file.ext". The base output dir is assumed to be "output". The return
     * value results in being "output/subdir/file.ext", where "output/subdir" will be created. The file itself will NOT
     * be created.
     * </p>
     *
     * @param mediaSource the media source, which requires at least 1 parent element.
     * @return the target as described.
     */
    Path createOutputDirectoryTree(Path mediaSource) {

        Path relativeParent = mediaSource.subpath(1, mediaSource.getNameCount());
        Path target = cleanupConfig.base_output_dir().resolve(relativeParent);
        FileUtil.createParentDirectoriesFor(target);
        return target;
    }
}
