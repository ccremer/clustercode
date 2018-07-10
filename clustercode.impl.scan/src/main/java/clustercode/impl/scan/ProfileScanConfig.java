package clustercode.impl.scan;

import clustercode.impl.scan.matcher.ProfileMatchers;
import org.aeonbits.owner.Config;

import java.nio.file.Path;
import java.util.List;

public interface ProfileScanConfig extends Config {

    /**
     * Gets the extension of the profile file name.
     *
     * @return the extension, with leading dot if applicable (e.g. ".ffmpeg"), not null.
     */
    @Key("CC_PROFILE_FILE_EXTENSION")
    @DefaultValue(".ffmpeg")
    String getProfileFileNameExtension();

    /**
     * Gets the base name of the profile file. This method could be combined with {@link #getProfileFileNameExtension()}
     * to create a file (e.g. "profile.ffmpeg").
     *
     * @return the file name (e.g. "profile"), not null.
     */
    @Key("CC_PROFILE_FILE_NAME")
    @DefaultValue("profile")
    String getProfileFileName();

    /**
     * Gets the root directory for profiles.
     *
     * @return the path to the directory, not null.
     */
    @Key("CC_PROFILE_DIR")
    @DefaultValue("/profiles")
    Path getProfilesBaseDir();

    /**
     * Gets the base name of the default profile file without extension. This method could be combined with {@link
     * #getProfileFileNameExtension()}.
     *
     * @return the file name (e.g. "default"), not null.
     */
    @Key("CC_PROFILE_FILE_DEFAULT")
    @DefaultValue("default")
    String getDefaultProfileFileName();

    @Key("CC_PROFILE_STRATEGY")
    @DefaultValue("COMPANION DIRECTORY_STRUCTURE DEFAULT")
    @Separator(" ")
    List<ProfileMatchers> profile_matchers();

}
