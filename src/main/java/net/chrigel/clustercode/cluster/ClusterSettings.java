package net.chrigel.clustercode.cluster;

public interface ClusterSettings {

    /**
     * Gets a number {@literal >= 1} after which any cleanup in the cluster gets removed from. This is used to remove
     * orphan tasks.
     *
     * @return the timeout in hours for removal.
     */
    long getTaskTimeout();
}
