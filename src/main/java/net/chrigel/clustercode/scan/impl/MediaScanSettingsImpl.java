package net.chrigel.clustercode.scan.impl;

import lombok.ToString;
import net.chrigel.clustercode.scan.MediaScanSettings;
import net.chrigel.clustercode.util.FilesystemProvider;
import net.chrigel.clustercode.util.InvalidConfigurationException;

import javax.inject.Inject;
import javax.inject.Named;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@ToString
class MediaScanSettingsImpl implements MediaScanSettings {

    private final String skip;
    private final Path baseInputDir;
    private final List<String> extensionList;
    private final long scanInterval;

    @Inject
    MediaScanSettingsImpl(@Named(ScanModule.MEDIA_INPUT_DIR_KEY) String baseDir,
                          @Named(ScanModule.MEDIA_EXTENSIONS_KEY) String extensions,
                          @Named(ScanModule.MEDIA_SKIP_NAME_KEY) String skip,
                          @Named(ScanModule.MEDIA_SCAN_INTERVAL_KEY) long scanInterval) {
        this.skip = skip;
        this.baseInputDir = FilesystemProvider.getInstance().getPath(baseDir);
        this.extensionList = new LinkedList<>(Arrays.asList(extensions.split(",")));
        checkInterval(scanInterval);
        this.scanInterval = scanInterval;
    }

    private void checkInterval(long scanInterval) {
        if (scanInterval < 1) {
            throw new InvalidConfigurationException("The scan interval must be >= 1.");
        }
    }

    @Override
    public Path getBaseInputDir() {
        return baseInputDir;
    }

    @Override
    public List<String> getAllowedExtensions() {
        return extensionList;
    }

    @Override
    public String getSkipExtension() {
        return skip;
    }

    @Override
    public long getMediaScanInterval() {
        return scanInterval;
    }
}
