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
 * Provides a matcher which looks in the recreated directory structure in the profiles folder based on the source file
 * of the media. For a media file such as {@code 0/movies/subdir/movie.mp4} this matcher will look for a profile in
 * {@code /profiles/0/movies/subdir/}. If it did not find it or on error, the parent will be searched ({@code
 * /profiles/0/movies/}). This matcher stops at the root directory of the input dir, in the example case it is {@code
 * 0/}.
 */
@XSlf4j
public class DirectoryStructureMatcher implements ProfileMatcher {

    private final ProfileParser profileParser;
    private final ProfileScanConfig profileScanConfig;

    @Inject
    DirectoryStructureMatcher(ProfileScanConfig profileScanConfig,
                              ProfileParser profileParser) {
        this.profileParser = profileParser;
        this.profileScanConfig = profileScanConfig;
    }

    @Override
    public Optional<Profile> apply(Media candidate) {
        log.entry(candidate);
        Path mediaFileParent = candidate.getSourcePath().getParent();
        Path sisterDir = profileScanConfig.profile_base_dir().resolve(mediaFileParent);
        Path profileFile = sisterDir.resolve(profileScanConfig.profile_file_name() + profileScanConfig
                .profile_file_name_extension());

        Path rootDir = profileScanConfig.profile_base_dir().resolve(mediaFileParent.getName(0));
        return log.exit(parseRecursive(profileFile, rootDir));
    }

    private Optional<Profile> parseRecursive(Path file, Path root) {
        if (Files.exists(file)) {
            Optional<Profile> result = profileParser.parseFile(file);
            if (result.isPresent()) {
                log.info("Found profile: {}", result.get().getLocation());
                return result;
            } else {
                return parseRecursive(getProfileFileFromParentDirectory(file,
                        profileScanConfig.profile_file_name() + profileScanConfig.profile_file_name_extension()),
                        root);
            }
        } else if (file.getParent().equals(root)) {
            log.debug("Did not find a suitable profile in any subdir of {}", root);
            return Optional.empty();
        } else {
            return parseRecursive(getProfileFileFromParentDirectory(file,
                    profileScanConfig.profile_file_name() + profileScanConfig.profile_file_name_extension()),
                    root);
        }
    }

    private Path getProfileFileFromParentDirectory(Path profileFile, String fileNameOfParent) {
        return profileFile.getParent().getParent().resolve(fileNameOfParent);
    }

}
