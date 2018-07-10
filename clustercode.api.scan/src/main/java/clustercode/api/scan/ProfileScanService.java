package clustercode.api.scan;

import clustercode.api.domain.Media;
import clustercode.api.domain.Profile;

import java.util.Optional;

public interface ProfileScanService {

    /**
     * This java doc is invalid currently, as {@link ProfileMatchers} allow custom
     * strategies.
     *
     * @param candidate the selected media job, not null. The instance will not be modified.
     * @return an empty profile if it could not be parsed for some reason. Otherwise contains the most appropriate
     * profile for the internally configured transcoder.
     */
    Optional<Profile> selectProfile(Media candidate);

}
