package net.chrigel.clustercode.scan.impl;

import net.chrigel.clustercode.scan.Profile;
import net.chrigel.clustercode.task.MediaCandidate;

import java.util.Optional;
import java.util.function.Function;

public interface ProfileMatcher extends Function<MediaCandidate, Optional<Profile>> {

    /**
     * Applies the matcher.
     *
     * @param candidate the function argument.
     * @return the profile if found, empty if not or error occurred.
     */
    @Override
    Optional<Profile> apply(MediaCandidate candidate);
}
