package clustercode.impl.util;

public interface Indexable<T> extends Comparable<T> {

    int getIndex();

    void setIndex(int index);

}
