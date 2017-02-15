package net.chrigel.clustercode.cluster;

public interface ClusterSettings {

    /**
     * Gets a number {@literal >= 1} after which any cleanup in the cluster gets removed from. This is used to remove
     * orphan tasks.
     *
     * @return the timeout in hours for removal.
     */
    long getTaskTimeout();

    /**
     * Indicates whether this current java process is an arbiter node. An arbiter node does not participate in the
     * cluster scheduling and transcoding process, but is just there to provide a cluster quorum.
     *
     * @return true if arbiter enabled, otherwise false.
     */
    boolean isArbiter();
}
