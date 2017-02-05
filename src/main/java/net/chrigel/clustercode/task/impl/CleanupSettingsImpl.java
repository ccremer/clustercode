package net.chrigel.clustercode.task.impl;

import net.chrigel.clustercode.task.CleanupSettings;
import net.chrigel.clustercode.util.FilesystemProvider;

import javax.inject.Inject;
import javax.inject.Named;
import java.nio.file.Path;

class CleanupSettingsImpl implements CleanupSettings {

    private final Path outputDirectory;
    private final boolean overwriteFiles;

    @Inject
    CleanupSettingsImpl(@Named(TaskModule.CLEANUP_OUTPUT_DIR_KEY) String outputDirectory,
                        @Named(TaskModule.CLEANUP_OUTPUT_OVERWRITE_KEY) boolean overwriteFiles) {
        this.outputDirectory = FilesystemProvider.getInstance().getPath(outputDirectory);
        this.overwriteFiles = overwriteFiles;
    }

    @Override
    public Path getOutputBaseDirectory() {
        return outputDirectory;
    }

    @Override
    public boolean overwriteFiles() {
        return overwriteFiles;
    }
}
