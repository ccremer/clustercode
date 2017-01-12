package net.chrigel.clustercode.util;

import java.util.function.Predicate;

public class PredicateUtil {

    private PredicateUtil() {
        // Prevent Instantiation
    }

    public static <R> Predicate<R> not(Predicate<R> predicate) {
        return predicate.negate();
    }

}
