package net.chrigel.clustercode.cleanup.impl;

import net.chrigel.clustercode.cleanup.CleanupSettings;
import net.chrigel.clustercode.util.FilesystemProvider;

import javax.inject.Inject;
import javax.inject.Named;
import java.nio.file.Path;

class CleanupSettingsImpl implements CleanupSettings {

    private final Path outputDirectory;
    private final boolean overwriteFiles;
    private final int userId;
    private final int groupId;

    @Inject
    CleanupSettingsImpl(@Named(CleanupModule.CLEANUP_OUTPUT_DIR_KEY) String outputDirectory,
                        @Named(CleanupModule.CLEANUP_OUTPUT_OVERWRITE_KEY) boolean overwriteFiles,
                        @Named(CleanupModule.CLEANUP_OWNER_USER_KEY) int userId,
                        @Named(CleanupModule.CLEANUP_OWNER_GROUP_KEY) int groupId) {
        this.outputDirectory = FilesystemProvider.getInstance().getPath(outputDirectory);
        this.overwriteFiles = overwriteFiles;
        this.userId = userId;
        this.groupId = groupId;
    }

    @Override
    public Path getOutputBaseDirectory() {
        return outputDirectory;
    }

    @Override
    public boolean overwriteFiles() {
        return overwriteFiles;
    }

    @Override
    public int getGroupId() {
        return groupId;
    }

    @Override
    public int getUserId() {
        return userId;
    }
}
