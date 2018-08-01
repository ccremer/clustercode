package clustercode.impl.cleanup.processor;

import clustercode.api.cleanup.CleanupContext;
import clustercode.impl.cleanup.CleanupConfig;
import clustercode.impl.util.FileUtil;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Provides a processor which marks the source file as done in a designated directory, so that it would not be
 * rescheduled for transcoding during the next scan.
 */
public class MarkSourceDirProcessor
    extends AbstractMarkSourceProcessor {

    @Inject
    MarkSourceDirProcessor(CleanupConfig cleanupSettings) {
        super(cleanupSettings);
    }

    @Override
    protected CleanupContext doProcessStep(CleanupContext context) {
        Path source = getSourcePath(context);

        Path marked = createOutputDirectoryTree(
            cleanupConfig.base_input_dir().relativize(source.resolveSibling(
                source.getFileName().toString() + cleanupConfig.skip_extension())));

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

    /**
     * Creates the directory tree in {@link CleanupConfig#mark_source_dir()} using the given path. The given
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
        Path target = cleanupConfig.mark_source_dir().resolve(mediaSource);
        FileUtil.createParentDirectoriesFor(target);
        return target;
    }

}
