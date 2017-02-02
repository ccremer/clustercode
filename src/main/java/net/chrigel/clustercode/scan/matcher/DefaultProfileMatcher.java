package net.chrigel.clustercode.scan.matcher;

import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.scan.Profile;
import net.chrigel.clustercode.scan.ProfileParser;
import net.chrigel.clustercode.scan.ProfileScanSettings;
import net.chrigel.clustercode.scan.ProfileMatcher;
import net.chrigel.clustercode.task.Media;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Provides a matcher which looks for the global default profile.
 */
@XSlf4j
class DefaultProfileMatcher implements ProfileMatcher {

    private final ProfileParser parser;
    private final ProfileScanSettings settings;

    @Inject
    DefaultProfileMatcher(ProfileParser parser,
                          ProfileScanSettings settings) {
        this.parser = parser;
        this.settings = settings;
    }

    @Override
    public Optional<Profile> apply(Media candidate) {
        log.entry(candidate);
        Path profileFile = settings.getProfilesBaseDir().resolve(
                settings.getDefaultProfileFileName() + settings.getProfileFileNameExtension());
        if (Files.exists(profileFile)) {
            return log.exit(parser.parseFile(profileFile));
        } else {
            log.warn("Default profile file {} does not exist.", profileFile);
            return log.exit(Optional.empty());
        }
    }
}
