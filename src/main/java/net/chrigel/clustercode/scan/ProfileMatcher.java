package net.chrigel.clustercode.scan;

import java.util.Optional;
import java.util.function.Function;

public interface ProfileMatcher extends Function<Media, Optional<Profile>> {

    /**
     * Applies the matcher.
     *
     * @param candidate the function argument.
     * @return the profile if found, empty if not or error occurred.
     */
    @Override
    Optional<Profile> apply(Media candidate);
}
