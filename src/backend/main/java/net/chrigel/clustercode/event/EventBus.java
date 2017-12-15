package net.chrigel.clustercode.event;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Represents an event bus for a common event type.
 *
 * @param <B> the base event type ("topic").
 */
public interface EventBus<B extends Serializable> {

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
     * Fires the event to the bus. All subscribers will receive the specified event object. Processing the event will
     * be handled in a separate thread. This method is a blocking shortcut for {@link #emitAsync(E)}. If a subscriber
     * throws a runtime exception, it will be logged and existing answers will be kept, but no further subscribers will
     * be notified.
     *
     * @param payload the payload object.
     * @return a response from the subscribers, which enables RPC-alike one-to-many communication.
     */
    <E extends B, R> Response<R> emit(E payload);

    /**
     * Fires the event to the bus asynchronously. All subscribers will receive the specified event object in a single
     * separate thread. If a subscriber throws a runtime exception, it will be logged and existing answers will be kept,
     * but no further subscribers will be notified.
     *
     * @param payload the payload object.
     * @return the future holding the response from the subscribers, which enables RPC-alike one-to-many communication.
     */
    <E extends B, R> CompletableFuture<Response<R>> emitAsync(E payload);

}
