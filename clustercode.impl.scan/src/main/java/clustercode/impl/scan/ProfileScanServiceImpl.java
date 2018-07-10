package clustercode.impl.scan;

import clustercode.api.domain.Media;
import clustercode.api.domain.Profile;
import clustercode.api.scan.ProfileMatcher;
import clustercode.api.scan.ProfileMatcherStrategy;
import clustercode.api.scan.ProfileScanService;
import lombok.extern.slf4j.XSlf4j;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.Optional;

@XSlf4j
public class ProfileScanServiceImpl implements ProfileScanService {

    private final ProfileMatcherStrategy strategy;

    @Inject
    ProfileScanServiceImpl(ProfileMatcherStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public Optional<Profile> selectProfile(Media candidate) {
        log.entry(candidate);
        log.debug("Selecting a profile...");
        Iterator<ProfileMatcher> itr = strategy.matcherIterator();
        while (itr.hasNext()) {
            Optional<Profile> result = itr.next().apply(candidate);
            if (result.isPresent()) {
                log.info("Selected profile {} for {}.", result.get().getLocation(), candidate);
                return log.exit(result);
            }
        }
        log.info("Could not find a suitable profile for {}.", candidate);
        return log.exit(Optional.empty());
    }

}
