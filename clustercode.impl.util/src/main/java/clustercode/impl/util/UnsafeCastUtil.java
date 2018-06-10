package clustercode.impl.util;

/**
 *
 */
public class UnsafeCastUtil {

    private UnsafeCastUtil() {
        /* not instantiable */
    }

    /**
     * Warning! Using this method is a sin against the gods of programming!
     *
     * @param <T>
     * @param o
     * @return the casted object
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object o) {
        return (T) o;
    }
}
