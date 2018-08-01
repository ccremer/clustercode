package clustercode.impl.util.di;

import clustercode.impl.util.EnumeratedImplementation;
import clustercode.impl.util.InvalidConfigurationException;
import com.google.inject.multibindings.Multibinder;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class ModuleHelper {

    private ModuleHelper() {
    }

    /**
     * Sorts the given set of implementations according to the keys in the given string. This method may be used in
     * conjunction with configurable strategies, where the order matters and the values are enumerated.
     *
     * @param enums           a non-null list which contains the enums in the correct order.
     * @param implementations a set of implementations of the same interface T but with different concrete classes
     *                        (e.g. injected by a DI framework).
     * @param enumAccessor    a function which gets a single string value1 in upper case and returns the concrete class.
     *                        This can be used for enums such as {@code YourEnumType::valueOf}. See {@link
     *                        EnumeratedImplementation#getImplementingClass()}.
     * @param <T>             the interface type.
     * @param <C>             the implementation enum.
     * @return a new list where the concrete objects are sorted according to their classes specified in {@code key}.
     */
    public static <T, C extends EnumeratedImplementation<T>, E extends Enum> SortedSet<T>
    sortImplementations(List<E> enums,
                        Set<T> implementations,
                        Function<String, C>
                                enumAccessor) {
        Map<Class<? extends T>, Integer> strategies = new HashMap<>();
        int count = 0;
        for (E template : enums) {
            strategies.put(enumAccessor.apply(template.name()).getImplementingClass(), count++);
        }
        SortedSet<T> sortedList = new TreeSet<>(new ClassComparator<>(strategies));
        sortedList.addAll(implementations);
        return sortedList;
    }

    /**
     * Binds all occurrences of T to an implementation C using the guice multibinder.
     *
     * @param multibinder     the multibinder from guice.
     * @param implementations the array of implementation classes (e.g. {@code YourEnumType::values()}).
     * @param <I>             the interface type.
     * @param <C>             the enum type which returns the implementation classes for T.
     */
    public static <I, C extends EnumeratedImplementation<I>> void bindAll(Multibinder<I> multibinder,
                                                                          Supplier<C[]> implementations) {
        Arrays.asList(implementations.get()).forEach(impl ->
                multibinder.addBinding().to(impl.getImplementingClass())
        );
    }

    public static <T, E extends EnumeratedImplementation<T>> void bindStrategies(List<E> strategies, Multibinder<T>
            binder) {
        strategies.forEach(entry ->
                binder.addBinding()
                      .to(entry.getImplementingClass()));
    }

    /**
     * Binds all occurrences of T to an implementation C using the guice multibinder.
     *
     * @param multibinder     the multibinder from guice.
     * @param implementations the array of implementation classes (e.g. {@code YourEnumType::values()}).
     * @param scope           the scope.
     * @param <I>             the interface type.
     * @param <C>             the enum type which returns the implementation classes for T.
     */
    public static <I, C extends EnumeratedImplementation<I>> void bindAll(Multibinder<I> multibinder,
                                                                          Supplier<C[]> implementations,
                                                                          Class<? extends Annotation> scope) {
        Arrays.asList(implementations.get()).forEach(impl ->
                multibinder.addBinding().to(impl.getImplementingClass()).in(scope)
        );
    }

    /**
     * Binds all occurrences of T to an implementation C using the guice multibinder.
     *
     * @param multibinder     the multibinder from guice.
     * @param implementations the array of implementation classes (e.g. {@code YourEnumType::values()}).
     * @param <I>             the interface type.
     * @param <C>             the enum type which returns the implementation classes for T.
     */
    public static <I, C extends EnumeratedImplementation<I>> void bindAllAsEagerSingleton(
            Multibinder<I> multibinder,
            Supplier<C[]> implementations) {
        Arrays.asList(implementations.get()).forEach(impl ->
                multibinder.addBinding().to(impl.getImplementingClass()).asEagerSingleton()
        );
    }

    private static class ClassComparator<T> implements Comparator<T> {

        private final Map<Class<? extends T>, Integer> strategies;

        private ClassComparator(Map<Class<? extends T>, Integer> strategies) {
            this.strategies = strategies;
        }

        @Override
        public int compare(T o1, T o2) {
            int index1 = strategies.get(o1.getClass());
            int index2 = strategies.get(o2.getClass());
            return Integer.compare(index1, index2);
        }
    }

    public static <E extends Enum> StrategiesCheckerIntermediate<E> verifyIn(List<E> strategies) {
        return new StrategiesCheckerIntermediate<>(strategies);
    }

    public static class StrategiesCheckerIntermediate<E extends Enum> {

        private final List<E> strategies;

        private StrategiesCheckerIntermediate(List<E> strategies) {
            this.strategies = strategies;
        }

        public StrategiesChecker<E> that(E value1) {
            return new StrategiesChecker<>(value1, strategies);
        }
    }

    public static class StrategiesChecker<E extends Enum> {

        private final E value1;
        private final List<E> strategies;

        private StrategiesChecker(E value1, List<E> strategies) {
            this.value1 = value1;
            this.strategies = strategies;
        }

        /**
         * Checks if value1 is before value2 in the previously given {@code strategies} list. If it is not, an {@link
         * InvalidConfigurationException} is being thrown. Only checks if both values are present. It is assumed that
         * the
         * provided values are distinguishable (and not e.g. substring of each other).
         *
         * @param value2 the second value1.
         */
        public void isBefore(E value2) {
            if (strategies.contains(value1) && strategies.contains(value2)) {
                int index1 = strategies.indexOf(value1);
                int index2 = strategies.indexOf(value2);
                if (index1 >= index2) {
                    throw new InvalidConfigurationException(
                            "{} cannot be specified before {}. You configured: {}",
                            value2, value1, strategies);
                }
            }
        }

        /**
         * Checks if value1 AND value2 are in the previously given {@code strategies} list. If they are, an {@link
         * InvalidConfigurationException} is being thrown.
         *
         * @param value2 the second value.
         */
        public void isNotGivenTogetherWith(E value2) {
            if (strategies.contains(value1) && strategies.contains(value2)) {
                throw new InvalidConfigurationException(
                        "Config cannot contain {} and {} at the same time as they are incompatible. You configured: {}",
                        value1, value2, strategies);
            }
        }
    }
}
