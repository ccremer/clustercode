package net.chrigel.clustercode.scan.impl;

import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.scan.Profile;
import net.chrigel.clustercode.scan.ProfileScanService;
import net.chrigel.clustercode.task.Media;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.Optional;

@XSlf4j
class ProfileScanServiceImpl implements ProfileScanService {

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
