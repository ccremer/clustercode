package net.chrigel.clustercode.event;

import lombok.Synchronized;
import lombok.ToString;
import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.util.UnsafeCastUtil;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Provides a way to return values from subscribes which got notified by events.
 *
 * @param <T> The expected return type.
 */
@ToString(exclude = "completed")
@XSlf4j
public class Response<T> {

    private final List<T> answers = new LinkedList<>();
    private boolean completed;

    Response() {
        // prevent public construction.
    }

    @Synchronized
    void addAnswer(Object answer) {
        if (!completed && answer != null) answers.add(UnsafeCastUtil.cast(answer));
    }

    /**
     * Gets the first arbitrary answer from a subscriber. There may be no answer at all and the answer may not even be
     * of type T.
     *
     * @return the optional answer.
     */
    public Optional<T> getAnswer() {
        return answers.stream().findFirst();
    }

    /**
     * Gets the first arbitrary answer from a subscriber of the given type. All answers will be filtered using {@link
     * Class#isInstance(Object)}, so subtypes will be included as well. There may not ben answer, but if there is, it
     * will be of type {@code ofType}.
     *
     * @param ofType the type to filter for.
     * @param <R>    the return type.
     * @return an optional describing the value.
     */
    public <R> Optional<R> getAnswer(Class<R> ofType) {
        return getAnswers(ofType).stream().findFirst();
    }

    /**
     * Returns a collection of the answers given from the subscribers. This collection only holds the values that were
     * actually returned by a subscriber and does not contain null values.
     *
     * @return a collection of return values, may be empty.
     */
    public Collection<T> getAnswers() {
        return Collections.unmodifiableCollection(answers);
    }

    /**
     * Returns a collection of the answers of the given class. This collection only holds non-null values that were
     * actually returned by a subscriber. There may not be an answer of the given type at all, but if there is, it will
     * be of type {@code ofType}, including subtypes as described in {@link Class#isInstance(Object)}.
     *
     * @param ofType the type to filter for.
     * @param <R>    the return type.
     * @return a collection of return values, may be empty.
     */
    public <R> Collection<R> getAnswers(Class<R> ofType) {
        return UnsafeCastUtil.cast(answers.stream().filter(ofType::isInstance).collect(Collectors.toList()));
    }

    /**
     * Applies the given consumer for the first arbitrary answer of the given type. The given consumer will not be
     * called if there is no answer. This is a shortcut for {@code getAnswer(ofType).ifPresent(consumer)}.
     *
     * @param ofType   the type to filter for.
     * @param consumer the consumer.
     * @return this.
     */
    public Response<T> thenApplyFor(Class<T> ofType, Consumer<T> consumer) {
        getAnswer(ofType).ifPresent(consumer);
        return this;
    }

    /**
     * Applies the given consumer for each answer of the given type. The given consumer will not be called if there is
     * no
     * answer. This is a shortcut for {@code getAnswers(ofType).ifPresent(consumer)}
     *
     * @param ofType   the type to filter for.
     * @param consumer the consumer.
     * @return this.
     */
    public Response<T> thenApplyToAll(Class<T> ofType, Consumer<T> consumer) {
        getAnswers(ofType).forEach(consumer);
        return this;
    }

    @Synchronized
    Response<T> setComplete() {
        completed = true;
        return this;
    }
}
