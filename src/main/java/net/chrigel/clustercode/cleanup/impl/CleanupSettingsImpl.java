package net.chrigel.clustercode.cleanup.impl;

import com.google.inject.Inject;
import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.cleanup.CleanupSettings;
import net.chrigel.clustercode.util.FilesystemProvider;

import javax.inject.Named;
import java.nio.file.Path;
import java.util.Optional;

@XSlf4j
class CleanupSettingsImpl implements CleanupSettings {

    private final Path outputDirectory;
    private final boolean overwriteFiles;
    private Integer userId;
    private Integer groupId;
    private final Path markSourceDirectory;

    @Inject
    CleanupSettingsImpl(@Named(CleanupModule.CLEANUP_OUTPUT_DIR_KEY) String outputDirectory,
                        @Named(CleanupModule.CLEANUP_OUTPUT_OVERWRITE_KEY) boolean overwriteFiles,
                        @Named(CleanupModule.CLEANUP_MARK_SOURCE_DIR_KEY) String markSourceDir) {
        this.outputDirectory = FilesystemProvider.getInstance().getPath(outputDirectory);
        this.markSourceDirectory = FilesystemProvider.getInstance().getPath(markSourceDir);
        this.overwriteFiles = overwriteFiles;
    }

    @Inject(optional = true)
    void setUserId(@Named(CleanupModule.CLEANUP_OWNER_USER_KEY) Integer userId) {
        this.userId = userId;
    }

    @Inject(optional = true)
    void setGroupId(@Named(CleanupModule.CLEANUP_OWNER_GROUP_KEY) Integer groupId) {
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
    public Optional<Integer> getGroupId() {
        return Optional.ofNullable(groupId);
    }

    @Override
    public Optional<Integer> getUserId() {
        return Optional.ofNullable(userId);
    }

    @Override
    public Path getMarkSourceDirectory() {
        return markSourceDirectory;
    }
}
