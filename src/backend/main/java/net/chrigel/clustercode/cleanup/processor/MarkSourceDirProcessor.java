package net.chrigel.clustercode.cleanup.processor;

import net.chrigel.clustercode.cleanup.CleanupContext;
import net.chrigel.clustercode.cleanup.CleanupProcessor;
import net.chrigel.clustercode.cleanup.CleanupSettings;
import net.chrigel.clustercode.cleanup.impl.CleanupModule;
import net.chrigel.clustercode.scan.MediaScanSettings;
import net.chrigel.clustercode.util.FileUtil;
import net.chrigel.clustercode.util.InvalidConfigurationException;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Provides a processor which marks the source file as done in a designated directory, so that it would not be
 * rescheduled for transcoding during the next scan.
 */
public class MarkSourceDirProcessor
    extends AbstractMarkSourceProcessor
    implements CleanupProcessor {

    private final CleanupSettings cleanupSettings;

    @Inject
    MarkSourceDirProcessor(MediaScanSettings mediaScanSettings,
                           CleanupSettings cleanupSettings) {
        super(mediaScanSettings);
        this.cleanupSettings = cleanupSettings;
        checkSettings();
    }

    private void checkSettings() {
        if (!cleanupSettings.getMarkSourceDirectory().isPresent()) {
            RuntimeException ex = new InvalidConfigurationException(
                "{} is not set. You cannot use the {} strategy without setting this directory.",
                CleanupModule.CLEANUP_MARK_SOURCE_DIR_KEY, CleanupProcessors.MARK_SOURCE_DIR.name());
            log.error(ex.getMessage());
            throw ex;
        }
    }

    @Override
    protected CleanupContext doProcessStep(CleanupContext context) {
        Path source = getSourcePath(context);

        Path marked = createOutputDirectoryTree(
            mediaScanSettings.getBaseInputDir().relativize(source.resolveSibling(
                source.getFileName().toString() + mediaScanSettings.getSkipExtension())));

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
     * Creates the directory tree in {@link CleanupSettings#getMarkSourceDirectory()} using the given path. The given
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
        Path target = cleanupSettings.getMarkSourceDirectory().orElse(Paths.get("")).resolve(mediaSource);
        FileUtil.createParentDirectoriesFor(target);
        return target;
    }

}
