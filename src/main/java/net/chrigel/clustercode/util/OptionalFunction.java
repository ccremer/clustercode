package net.chrigel.clustercode.util;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@FunctionalInterface
public interface OptionalFunction<T, R> extends Function<T, Optional<R>> {

    /**
     * Creates function that returns an empty optional. This is used to consume the parameter without returning a value.
     *
     * @param consumer
     * @param <E>
     * @param <R>
     * @return a new function.
     */
    static <E, R> OptionalFunction<E, R> returningEmpty(Consumer<E> consumer) {
        return event -> {
            consumer.accept(event);
            return Optional.empty();
        };
    }

    /**
     * Creates a function that wraps the return value of the given function in an Optional. The given function may
     * return null.
     *
     * @param function
     * @param <E>
     * @param <R>
     * @return a new function.
     */
    static <E, R> OptionalFunction<E, R> toOptional(Function<E, R> function) {
        return event -> Optional.ofNullable(function.apply(event));
    }
}
