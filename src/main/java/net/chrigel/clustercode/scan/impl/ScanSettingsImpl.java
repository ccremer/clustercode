package net.chrigel.clustercode.scan.impl;

import net.chrigel.clustercode.scan.ScanSettings;
import net.chrigel.clustercode.util.FilesystemProvider;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ScanSettingsImpl implements ScanSettings {

    private final String skip;
    private final Path baseInputDir;
    private final List<String> extensionList;

    @Inject
    ScanSettingsImpl(String baseDir, String extensions, String skip) {
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
