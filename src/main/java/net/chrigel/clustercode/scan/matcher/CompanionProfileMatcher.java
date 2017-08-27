package net.chrigel.clustercode.scan.matcher;

import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.scan.*;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Provides a matcher which will search for a file named exactly as the media file, but with an additional extension for
 * the configured transcoder settings defined in {@link ProfileScanSettings}.
 */
@XSlf4j
class CompanionProfileMatcher implements ProfileMatcher {

    private final ProfileScanSettings scanSettings;
    private final ProfileParser profileParser;

    @Inject
    CompanionProfileMatcher(ProfileScanSettings scanSettings,
                            ProfileParser profileParser) {
        this.scanSettings = scanSettings;
        this.profileParser = profileParser;
    }

    @Override
    public Optional<Profile> apply(Media candidate) {
        log.entry(candidate);
        Path profile = candidate.getSourcePath().resolveSibling(
                candidate.getSourcePath().getFileName() + scanSettings.getProfileFileNameExtension());

        if (Files.exists(profile)) {
            return log.exit(profileParser.parseFile(profile));
        } else {
            log.debug("Companion file {} does not exist.", profile);
            return log.exit(Optional.empty());
        }
    }
}
