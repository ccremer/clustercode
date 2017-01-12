package net.chrigel.clustercode.scan;

import java.nio.file.Path;

public interface ProfileScanSettings {

    String getProfileFileNameExtension();

    String getProfileFileName();

    /**
     * Gets the root directory for profiles.
     *
     * @return the path to the directory, not null.
     */
    Path getProfilesBaseDir();
}
