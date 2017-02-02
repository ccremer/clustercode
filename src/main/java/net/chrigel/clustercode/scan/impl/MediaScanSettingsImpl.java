package net.chrigel.clustercode.scan.impl;

import lombok.ToString;
import net.chrigel.clustercode.scan.MediaScanSettings;
import net.chrigel.clustercode.util.FilesystemProvider;

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

    @Inject
    MediaScanSettingsImpl(@Named(ScanModule.MEDIA_INPUT_DIR_KEY) String baseDir,
                          @Named(ScanModule.MEDIA_EXTENSIONS_KEY) String extensions,
                          @Named(ScanModule.MEDIA_SKIP_NAME_KEY) String skip) {
        this.skip = skip;
        this.baseInputDir = FilesystemProvider.getInstance().getPath(baseDir);
        this.extensionList = new LinkedList<>(Arrays.asList(extensions.split(",")));
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
}
