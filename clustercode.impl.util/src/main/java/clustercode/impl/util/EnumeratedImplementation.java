package clustercode.impl.util;

public interface EnumeratedImplementation<T> {

    /**
     * Gets the implementation (concrete) class of T.
     *
     * @return the concrete class which implements or extends T.
     */
    Class<? extends T> getImplementingClass();

    void setImplementingClass(Class<? extends T> clazz);
}
