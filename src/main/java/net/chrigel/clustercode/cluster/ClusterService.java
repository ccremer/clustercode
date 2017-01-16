package net.chrigel.clustercode.cluster;

import net.chrigel.clustercode.task.MediaCandidate;

public interface ClusterService {

    /**
     * Joins the cluster. If this Java process is the first member, it will create a new cluster. If a cluster cannot be
     * created, it will downgrade to a single-node cluster.
     */
    void joinCluster();

    /**
     * Leaves the cluster, if it was connected.
     */
    void leaveCluster();

    /**
     * Sets the task which is being executed by this Java process. Replaces the old task if present, only one task can
     * be active. Tasks which are long in the cluster than {@link ClusterSettings#getTaskTimeout()} are being removed.
     * This method does nothing if not connected to the cluster.
     *
     * @param candidate the task, not null.
     */
    void setTask(MediaCandidate candidate);

    /**
     * Removes the currently active task from the cluster, if there was one set.
     */
    void removeTask();

    /**
     * Returns true if the candidate is known across the cluster. If this Java process is the only member or not at all
     * in the cluster, it returns false.
     *
     * @param candidate the candidate, not null.
     * @return true if queued.
     */
    boolean isQueuedInCluster(MediaCandidate candidate);
}
