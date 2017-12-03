package net.chrigel.clustercode.cleanup.processor;

import net.chrigel.clustercode.cleanup.CleanupContext;
import net.chrigel.clustercode.cleanup.CleanupProcessor;
import net.chrigel.clustercode.cleanup.CleanupSettings;
import net.chrigel.clustercode.scan.MediaScanSettings;
import net.chrigel.clustercode.util.FileUtil;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Provides a processor which marks the source file as done in a designated directory, so that it would not be
 * rescheduled for transcoding during the next scan.
 */
public class MarkSourceDirProcessor
    extends AbstractMarkSourceProcessor
    implements CleanupProcessor {

    private final MediaScanSettings mediaScanSettings;
    private final CleanupSettings cleanupSettings;

    @Inject
    MarkSourceDirProcessor(MediaScanSettings mediaScanSettings,
                           CleanupSettings cleanupSettings) {
        this.mediaScanSettings = mediaScanSettings;
        this.cleanupSettings = cleanupSettings;
    }

    @Override
    public CleanupContext processStep(CleanupContext context) {
        log.entry(context);
        Path source = mediaScanSettings.getBaseInputDir().resolve(
            context.getTranscodeResult().getMedia().getSourcePath());

        if (!context.getTranscodeResult().isSuccessful()) {
            log.warn("Not marking {} as done, since transcoding failed.", source);
            return log.exit(context);
        }

        if (!Files.exists(source)) {
            log.warn("Not marking {} as done, since the file does not exist (anymore).", source);
            return log.exit(context);
        }

        Path marked = createOutputDirectoryTree(
            mediaScanSettings.getBaseInputDir().relativize(source.resolveSibling(
                source.getFileName().toString() + mediaScanSettings.getSkipExtension())));

        createMarkFile(marked, source);
        return log.exit(context);
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
        Path target = cleanupSettings.getMarkSourceDirectory().resolve(mediaSource);
        FileUtil.createParentDirectoriesFor(target);
        return target;
    }

}
