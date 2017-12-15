package net.chrigel.clustercode.cluster;

public interface ClusterSettings {

    /**
     * Indicates whether this current java process is an arbiter node. An arbiter node does not participate in the
     * cluster scheduling and transcoding process, but is just there to provide a cluster quorum.
     *
     * @return true if arbiter enabled, otherwise false.
     */
    boolean isArbiter();
}
