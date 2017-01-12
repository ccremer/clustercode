package net.chrigel.clustercode.scan.impl;

import java.util.Iterator;

public interface ProfileMatcherStrategy {

    /**
     * Gets the iterator for the profile matchers. They elements are in order of the implementation strategy.
     *
     * @return the iterator, not null.
     */
    Iterator<ProfileMatcher> matcherIterator();

}
