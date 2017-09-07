package net.chrigel.clustercode.event;

import java.util.function.Consumer;

/**
 * Represents an event bus for a common event type.
 * @param <B> the base event type.
 */
public interface EventBus<B> {

    /**
     * Register a subscriber for a specific event class. Is a no-op if it already is subscribed.
     *
     * @param subscriber the subscriber.
     */
    <E extends B> void registerEventHandler(Class<? extends B> clazz, Consumer<Event<E>> subscriber);

    /**
     * Unregister a subscriber for the specified event class. Is a no-op if it doesn't exist.
     *
     * @param subscriber the subscriber.
     */
    <E extends B> void unRegister(Class<? extends B> clazz, Consumer<Event<E>> subscriber);

    /**
     * Fires the event to the bus. All subscribers will receive the specified event object.
     * @param event the event object.
     */
    <E extends B> void emit(Event<E> event);
}
