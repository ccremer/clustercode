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
 * Provides a matcher which will search for a file named exactly as the media file, but with an additional extension for
 * the configured transcoder settings defined in {@link ProfileScanConfig}.
 */
@XSlf4j
public class CompanionProfileMatcher implements ProfileMatcher {

    private final ProfileScanConfig scanConfig;
    private final ProfileParser profileParser;

    @Inject
    CompanionProfileMatcher(ProfileScanConfig scanConfig,
                            ProfileParser profileParser) {
        this.scanConfig = scanConfig;
        this.profileParser = profileParser;
    }

    @Override
    public Optional<Profile> apply(Media candidate) {
        log.entry(candidate);
        Path profile = candidate.getSourcePath().resolveSibling(
                candidate.getSourcePath().getFileName() + scanConfig.profile_file_name_extension());

        if (Files.exists(profile)) {
            return log.exit(profileParser.parseFile(profile));
        } else {
            log.debug("Companion file {} does not exist.", profile);
            return log.exit(Optional.empty());
        }
    }
}
