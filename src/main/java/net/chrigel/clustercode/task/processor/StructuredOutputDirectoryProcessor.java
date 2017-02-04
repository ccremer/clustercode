package net.chrigel.clustercode.task.processor;

import net.chrigel.clustercode.task.CleanupContext;
import net.chrigel.clustercode.task.CleanupProcessor;
import net.chrigel.clustercode.task.CleanupSettings;
import net.chrigel.clustercode.transcode.TranscodeResult;

import javax.inject.Inject;
import java.nio.file.Path;
import java.time.Clock;

/**
 * Provides a processor which recreates the source directory tree in the configured root output directory. The priority
 * directory will be omitted. If overwriting is disabled, the file will have a timestamp appended in the file name.
 */
public class StructuredOutputDirectoryProcessor
        extends AbstractOutputDirectoryProcessor
        implements CleanupProcessor {

    private final CleanupSettings cleanupSettings;

    @Inject
    StructuredOutputDirectoryProcessor(CleanupSettings cleanupSettings,
                                       Clock clock) {
        super(clock);
        this.cleanupSettings = cleanupSettings;
        createDirectoriesFor(cleanupSettings.getOutputBaseDirectory());
    }

    @Override
    public CleanupContext processStep(CleanupContext context) {

        TranscodeResult result = context.getTranscodeResult();

        Path source = result.getTemporaryPath();
        Path media = result.getMedia().getSourcePath();
        Path target = createOutputDirectoryTree(media);
        createParentDirectoriesFor(target);
        context.setOutputPath(moveAndReplaceExisting(source, target, cleanupSettings.overwriteFiles()));
        return context;
    }

    /**
     * Creates the directory tree in {@link CleanupSettings#getOutputBaseDirectory()} using the given path. The given
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
        Path target = cleanupSettings.getOutputBaseDirectory().resolve(relativeParent);
        createParentDirectoriesFor(target);
        return target;
    }
}
