package net.chrigel.clustercode.scan.impl;

import net.chrigel.clustercode.scan.ProfileScanSettings;
import net.chrigel.clustercode.util.FilesystemProvider;

import javax.inject.Inject;
import javax.inject.Named;
import java.nio.file.Path;

class ProfileScanSettingsImpl implements ProfileScanSettings {

    private final String profileFileNameExtension;
    private final String profileFileName;
    private final String defaultProfileFileName;
    private final Path profilesBaseDir;

    @Inject
    ProfileScanSettingsImpl(@Named(ScanModule.PROFILE_FILE_EXTENSION_KEY) String profileFileNameExtension,
                            @Named(ScanModule.PROFILE_FILE_NAME_KEY) String profileFileName,
                            @Named(ScanModule.PROFILE_DIRECTORY_KEY) String profileBaseDir,
                            @Named(ScanModule.PROFILE_FILE_DEFAULT_KEY) String defaultProfileFileName) {
        this.profileFileNameExtension = profileFileNameExtension;
        this.profileFileName = profileFileName;
        this.defaultProfileFileName = defaultProfileFileName;
        this.profilesBaseDir = FilesystemProvider.getInstance().getPath(profileBaseDir);
    }

    @Override
    public String getProfileFileNameExtension() {
        return profileFileNameExtension;
    }

    @Override
    public String getProfileFileName() {
        return profileFileName;
    }

    @Override
    public Path getProfilesBaseDir() {
        return profilesBaseDir;
    }

    @Override
    public String getDefaultProfileFileName() {
        return defaultProfileFileName;
    }

}
