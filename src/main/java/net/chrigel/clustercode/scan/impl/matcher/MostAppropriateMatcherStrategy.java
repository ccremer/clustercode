package net.chrigel.clustercode.scan.impl.matcher;

import net.chrigel.clustercode.scan.impl.ProfileMatcherStrategy;
import net.chrigel.clustercode.scan.impl.ProfileMatcher;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class MostAppropriateMatcherStrategy implements ProfileMatcherStrategy {

    private final List<ProfileMatcher> matchers;

    @Inject
    MostAppropriateMatcherStrategy(CompanionProfileMatcher companionProfileMatcher,
                                   DefaultProfileMatcher defaultProfileMatcher,
                                   DirectoryStructureMatcher directoryStructureMatcher) {
        this.matchers = Arrays.asList(
                companionProfileMatcher,
                directoryStructureMatcher,
                defaultProfileMatcher);
    }

    @Override
    public Iterator<ProfileMatcher> matcherIterator() {
        return matchers.iterator();
    }
}
