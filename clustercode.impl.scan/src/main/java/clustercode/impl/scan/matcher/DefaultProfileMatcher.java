package clustercode.impl.scan.matcher;

import clustercode.api.domain.Media;
import clustercode.api.domain.Profile;
import clustercode.api.scan.ProfileMatcher;
import clustercode.api.scan.ProfileParser;
import clustercode.impl.scan.ProfileScanConfig;
import lombok.extern.slf4j.XSlf4j;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Provides a matcher which looks for the global default profile.
 */
@XSlf4j
public class DefaultProfileMatcher implements ProfileMatcher {

    private final ProfileParser parser;
    private final ProfileScanConfig profileScanConfig;

    @Inject
    DefaultProfileMatcher(ProfileScanConfig profileScanConfig,
                          ProfileParser parser) {
        this.parser = parser;
        this.profileScanConfig = profileScanConfig;
    }

    @Override
    public Optional<Profile> apply(Media candidate) {
        log.entry(candidate);
        Path profileFile = profileScanConfig.profile_base_dir().resolve(
                profileScanConfig.default_profile_file_name() + profileScanConfig.profile_file_name_extension());
        if (Files.exists(profileFile)) {
            return log.exit(parser.parseFile(profileFile));
        } else {
            log.warn("Default profile file {} does not exist.", profileFile);
            return log.exit(Optional.empty());
        }
    }

}
