package net.chrigel.clustercode.scan.matcher;

import net.chrigel.clustercode.scan.ProfileMatcher;
import net.chrigel.clustercode.util.di.EnumeratedImplementation;

public enum Matchers implements EnumeratedImplementation<ProfileMatcher> {

    COMPANION(CompanionProfileMatcher.class),
    DEFAULT(DefaultProfileMatcher.class),
    DIRECTORY_STRUCTURE(DirectoryStructureMatcher.class);

    private final Class<? extends ProfileMatcher> implementingClass;

    Matchers(Class<? extends ProfileMatcher> clazz) {
        this.implementingClass = clazz;
    }

    @Override
    public Class<? extends ProfileMatcher> getImplementingClass() {
        return implementingClass;
    }

}
