package clustercode.impl.util;

import java.util.function.Predicate;

public final class PredicateUtil {

    private PredicateUtil() {
        // Prevent Instantiation
    }

    public static <R> Predicate<R> not(Predicate<R> predicate) {
        return predicate.negate();
    }

}
