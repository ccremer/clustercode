package clustercode.impl.scan;

import clustercode.api.domain.Media;
import clustercode.api.domain.Profile;
import clustercode.api.scan.ProfileMatcher;
import clustercode.api.scan.ProfileScanService;
import clustercode.impl.scan.matcher.ProfileMatchers;
import lombok.extern.slf4j.XSlf4j;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;

@XSlf4j
public class ProfileScanServiceImpl implements ProfileScanService {

    private final Map<ProfileMatchers, ProfileMatcher> profileMatcherMap;

    @Inject
    ProfileScanServiceImpl(Map<ProfileMatchers, ProfileMatcher> profileMatcherMap) {
        this.profileMatcherMap = profileMatcherMap;
    }

    @Override
    public Optional<Profile> selectProfile(Media candidate) {
        log.entry(candidate);
        log.debug("Selecting a profile...");
        for (ProfileMatcher profileMatcher : profileMatcherMap.values()) {
            Optional<Profile> result = profileMatcher.apply(candidate);
            if (result.isPresent()) {
                log.info("Selected profile {} for {}.", result.get().getLocation(), candidate);
                return log.exit(result);
            }
        }
        log.info("Could not find a suitable profile for {}.", candidate);
        return log.exit(Optional.empty());
    }

}
