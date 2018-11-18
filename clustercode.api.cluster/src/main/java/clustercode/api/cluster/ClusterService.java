package clustercode.api.cluster;

import clustercode.api.domain.Media;

import java.util.Optional;

public interface ClusterService {

    /**
     * Joins the cluster. If this Java process is the first member, it will create a new cluster. If a cluster cannot be
     * created, it will downgrade to a single-node cluster.
     */
    void joinCluster();

    /**
     * Removes the currently active task from the cluster, if there was one set.
     */
    void removeTask();

    /**
     * Sets the cleanup which is being executed by this Java process. Replaces the old cleanup if present, only one task
     * can be active.
     * This method does nothing if not connected to the cluster.
     *
     * @param candidate the candidate, not null.
     */
    void setTask(Media candidate);

    /**
     * Returns true if the candidate is known across the cluster. If this Java process is the only member or not at all
     * in the cluster, it returns false.
     *
     * @param candidate the candidate, not null.
     * @return true if queued.
     */
    boolean isQueuedInCluster(Media candidate);

    /**
     * Gets the name of the cluster node.
     *
     * @return the name, otherwise empty.
     */
    Optional<String> getName();

}
