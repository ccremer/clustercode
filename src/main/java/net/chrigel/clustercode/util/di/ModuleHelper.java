package net.chrigel.clustercode.util.di;

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
     * @param key             a non-null string which contains the names of enumerated classes. Multiple values have to
     *                        be separated with a single whitespace.
     * @param implementations a set of implementations of the same interface T but with different concrete classes
     *                        (e.g. injected by a DI framework).
     * @param enumAccessor    a function which gets a single string value in upper case and returns the concrete class.
     *                        This can be used for enums such as {@code YourEnumType::valueOf}. See {@link
     *                        EnumeratedImplementation#getImplementingClass()}.
     * @param <T>             the interface type.
     * @param <C>             the implementation enum.
     * @return a new list where the concrete objects are sorted according to their classes specified in {@code key}.
     */
    public static <T, C extends EnumeratedImplementation<T>> List<T> sortImplementations(String key,
                                                                                         Set<T> implementations,
                                                                                         Function<String, C>
                                                                                                 enumAccessor) {
        Map<Class<? extends T>, Integer> strategies = new HashMap<>();
        int count = 0;
        for (String strategy : getEnumValuesFromKey(key)) {
            strategies.put(enumAccessor.apply(strategy).getImplementingClass(), count++);
        }
        List<T> sortedList = new LinkedList<>(implementations);
        sortedList.sort(new ClassComparator<>(strategies));
        return sortedList;
    }

    /**
     * Splits the given string by single whitespace and returns an upper cased array of the values.
     *
     * @param strategies the non-null string.
     * @return an array of the values, as specified in {@link String#split(String)}.
     */
    public static String[] getEnumValuesFromKey(String strategies) {
        return strategies.trim().toUpperCase(Locale.ENGLISH).split(" ");
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

    /**
     * Binds the enumerated values in the given key to their implementation classes.
     *
     * @param multibinder  the multibinder from guice.
     * @param key          the single whitespace separated string of Enum values. See {@link
     *                     #getEnumValuesFromKey(String)}.
     * @param enumAccessor the function which will return the implementation class for a given substring of {@code
     *                     key}.
     * @param <T>          the interface type.
     */
    public static <T> void bindStrategies(Multibinder<T> multibinder, String key,
                                          Function<String, EnumeratedImplementation<T>> enumAccessor) {
        for (String strategy : getEnumValuesFromKey(key)) {
            multibinder.addBinding().to(enumAccessor.apply(strategy).getImplementingClass());
        }
    }

    /**
     * Binds the enumerated values in the given key to their implementation classes.
     *
     * @param multibinder  the multibinder from guice.
     * @param key          the single whitespace separated string of Enum values. See {@link
     *                     #getEnumValuesFromKey(String)}.
     * @param enumAccessor the function which will return the implementation class for a given substring of {@code
     *                     key}.
     * @param scope        the scope in which guice should bind the implementations.
     * @param <T>          the interface type.
     */
    public static <T> void bindStrategies(Multibinder<T> multibinder, String key,
                                          Function<String, EnumeratedImplementation<T>> enumAccessor,
                                          Class<? extends Annotation> scope) {
        for (String strategy : getEnumValuesFromKey(key)) {
            multibinder.addBinding().to(enumAccessor.apply(strategy).getImplementingClass()).in(scope);
        }
    }

    /**
     * Binds the enumerated values in the given key to their implementation classes as eager singleton.
     *
     * @param multibinder  the multibinder from guice.
     * @param key          the single whitespace separated string of Enum values. See {@link
     *                     #getEnumValuesFromKey(String)}.
     * @param enumAccessor the function which will return the implementation class for a given substring of {@code
     *                     key}.
     * @param <T>          the interface type.
     */
    public static <T> void bindStrategiesAsEagerSingleton(Multibinder<T> multibinder, String key,
                                                          Function<String, EnumeratedImplementation<T>> enumAccessor) {
        for (String strategy : getEnumValuesFromKey(key)) {
            multibinder.addBinding().to(enumAccessor.apply(strategy).getImplementingClass()).asEagerSingleton();
        }
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

}
