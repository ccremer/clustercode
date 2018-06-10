package net.chrigel.clustercode.scan;

import java.nio.file.Path;

public interface ProfileScanSettings {

    /**
     * Gets the extension of the profile file name.
     *
     * @return the extension, with leading dot if applicable (e.g. ".ffmpeg"), not null.
     */
    String getProfileFileNameExtension();

    /**
     * Gets the base name of the profile file. This method could be combined with {@link #getProfileFileNameExtension()}
     * to create a file (e.g. "profile.ffmpeg").
     *
     * @return the file name (e.g. "profile"), not null.
     */
    String getProfileFileName();

    /**
     * Gets the root directory for profiles.
     *
     * @return the path to the directory, not null.
     */
    Path getProfilesBaseDir();

    /**
     * Gets the base name of the default profile file without extension. This method could be combined with {@link
     * #getProfileFileNameExtension()}.
     *
     * @return the file name (e.g. "default"), not null.
     */
    String getDefaultProfileFileName();

}
