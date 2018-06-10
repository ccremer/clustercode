package net.chrigel.clustercode.cleanup;

import java.util.Iterator;

public interface CleanupStrategy {

    /**
     * Gets the iterator for the cleanup processors. They elements are in order of the implementation strategy.
     *
     * @return the iterator, not null.
     */
    Iterator<CleanupProcessor> processorIterator();

}
