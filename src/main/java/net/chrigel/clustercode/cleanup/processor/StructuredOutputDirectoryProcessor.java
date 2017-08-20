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
        FileUtil.createDirectoriesFor(cleanupSettings.getOutputBaseDirectory());
    }

    @Override
    public CleanupContext processStep(CleanupContext context) {
        log.entry(context);
        TranscodeResult result = context.getTranscodeResult();

        Path source = result.getTemporaryPath();

        Path media = result.getMedia().getSourcePath();

        Path target = createOutputDirectoryTree(media);

        Path tempFile = source.getFileName();
        Path finalPath = target.getParent().resolve(tempFile);
        FileUtil.createParentDirectoriesFor(finalPath);
        context.setOutputPath(moveAndReplaceExisting(source, finalPath, cleanupSettings.overwriteFiles()));
        return log.exit(context);
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
        FileUtil.createParentDirectoriesFor(target);
        return target;
    }
}
