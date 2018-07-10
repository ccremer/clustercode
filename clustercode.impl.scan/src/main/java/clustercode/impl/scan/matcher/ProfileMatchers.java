package clustercode.impl.scan.matcher;

import clustercode.api.scan.ProfileMatcher;
import clustercode.impl.util.EnumeratedImplementation;

public enum ProfileMatchers implements EnumeratedImplementation<ProfileMatcher> {

    COMPANION(CompanionProfileMatcher.class),
    DEFAULT(DefaultProfileMatcher.class),
    DIRECTORY_STRUCTURE(DirectoryStructureMatcher.class);

    private final Class<? extends ProfileMatcher> implementingClass;

    ProfileMatchers(Class<? extends ProfileMatcher> clazz) {
        this.implementingClass = clazz;
    }

    @Override
    public Class<? extends ProfileMatcher> getImplementingClass() {
        return implementingClass;
    }

    @Override
    public void setImplementingClass(Class<? extends ProfileMatcher> clazz) {

    }

}
