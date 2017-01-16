package net.chrigel.clustercode.cluster;

public interface ClusterSettings {

    /**
     * Gets the cluster name.
     *
     * @return the name of the cluster, not null.
     */
    String getClusterName();

    /**
     * Gets a number {@literal >= 1} after which any task in the cluster gets removed from. This is used to remove
     * orphan tasks.
     *
     * @return the timeout in hours for removal.
     */
    long getTaskTimeout();
}
