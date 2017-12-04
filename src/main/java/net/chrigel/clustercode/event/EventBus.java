package net.chrigel.clustercode.event;

import net.chrigel.clustercode.util.OptionalFunction;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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
    <E extends B, R> void registerEventHandler(Class<? extends B> clazz, OptionalFunction<Event<E>, R> subscriber);

    /**
     * Unregister a subscriber for the specified event class. Is a no-op if it doesn't exist.
     *
     * @param subscriber the subscriber.
     */
    <E extends B, R> void unRegister(Class<? extends B> clazz, OptionalFunction<Event<E>, R> subscriber);

    /**
     * Fires the event to the bus. All subscribers will receive the specified event object. This method blocks until all
     * subscribers processed the event.
     *
     * @param event the event object.
     */
    <E extends B> void emit(Event<E> event);

    /**
     * Fires the event to the bus asynchronously. All subscribers will receive the specified event object in a single
     * separate
     * thread.
     *
     * @param event the event object.
     * @return the future holding the state of the execution.
     */
    <E extends B> CompletableFuture<Void> emitAsync(Event<E> event);

    /**
     * Emits the given event and awaits the return value of a subscriber. If multiple subscribers handle the same event
     * type, it will return the first non-empty result. If no subscribers returned a value, an empty optional is
     * returned. This method is blocking. The subscribers may not return the value as R, the programmer is responsible
     * for using or casting the correct types.
     *
     * @param event the event object.
     * @param <R>   the expected return type.
     * @param <E>   the message type.
     * @return An optional holding the result.
     */
    <R, E extends B> Optional<R> emitAndGet(Event<E> event);

    /**
     * Emits the given event and awaits all return values of all subscribers. Subscribers that returned an empty
     * optional will be omitted from the collection. If no subscribers returned a value, the collection is empty. This
     * method is blocking. The subscribers may not return the value as R and even mix multiple types. The programmer is
     * responsible for using or casting the correct types.
     *
     * @param event the event object.
     * @param <R>   the expected return type.
     * @param <E>   the message type.
     * @return A collection holding the return values.
     */
    <R, E extends B> Collection<R> emitAndGetAll(Event<E> event);
}
