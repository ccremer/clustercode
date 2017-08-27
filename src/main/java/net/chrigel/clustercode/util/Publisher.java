package net.chrigel.clustercode.util;

import java.util.function.Consumer;

/**
 * Represents a publisher in a publish/subscribe pattern.
 *
 * @param <T> the payload type.
 */
public interface Publisher<T> {

    /**
     * Register a subscriber. Is a no-op if it already is subscribed.
     *
     * @param subscriber the subscriber.
     */
    void register(Consumer<T> subscriber);

    /**
     * Unregister a subscriber. Is a no-op if it doesn't exist.
     *
     * @param subscriber the subscriber.
     */
    void unRegister(Consumer<T> subscriber);

}
