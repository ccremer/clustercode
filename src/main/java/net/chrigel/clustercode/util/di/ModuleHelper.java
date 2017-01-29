package net.chrigel.clustercode.util.di;

import java.util.*;
import java.util.function.Function;

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
     *                        This can be used for enums such as {@code YourEnumType.valueOf(String)
     *                        .getImplementationClass()}. See {@link EnumeratedImplementation#getImplementingClass()}.
     * @param <T>             the interface type.
     * @return a new list where the concrete objects are sorted according to their classes specified in {@code key}.
     */
    public static <T> List<T> sortImplementations(String key,
                                                  Set<T> implementations,
                                                  Function<String, Class<? extends T>> enumAccessor) {
        Map<Class<? extends T>, Integer> strategies = new HashMap<>();
        int count = 0;
        for (String strategy : Arrays.asList(key.trim().toUpperCase(Locale.ENGLISH).split(" "))) {
            strategies.put(enumAccessor.apply(strategy), count++);
        }
        List<T> sortedList = new LinkedList<>(implementations);
        sortedList.sort(new ClassComparator<>(strategies));
        return sortedList;
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
