package net.chrigel.clustercode.cluster;

import net.chrigel.clustercode.scan.Media;

import java.util.List;
import java.util.Optional;

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
     * Removes the currently active task from the cluster, if there was one set.
     */
    void removeTask();

    /**
     * Cancels the current task.
     *
     * @param hostname the hostname of the node from which the task is to be cancelled. if null, it means localhost.
     * @return true if cancelled or no job active. False if otherwise.
     */
    boolean cancelTask(String hostname);

    /**
     * Gets the task that is or was scheduled for this node. This method is useful after an application crash where
     * the cluster knows which task was scheduled for this specific node. So this method returns the task
     * previously scheduled for this node, otherwise empty.
     *
     * @return an optional describing the task.
     */
    List<ClusterTask> getTasks();

    /**
     * Sets the cleanup which is being executed by this Java process. Replaces the old cleanup if present, only one task
     * can be active. Tasks which are longer in the cluster than {@link ClusterSettings#getTaskTimeout()} are being
     * removed.
     * This method does nothing if not connected to the cluster.
     *
     * @param candidate the candidate, not null.
     */
    void setTask(Media candidate);

    /**
     * Sets the progress of the current task. Does nothing if no task is defined.
     *
     * @param percentage the progress in percentage.
     */
    void setProgress(double percentage);

    /**
     * Returns true if the candidate is known across the cluster. If this Java process is the only member or not at all
     * in the cluster, it returns false.
     *
     * @param candidate the candidate, not null.
     * @return true if queued.
     */
    boolean isQueuedInCluster(Media candidate);

    /**
     * Gets the amount of current cluster members. Includes arbiter nodes.
     *
     * @return cluster size. 0 if not in a cluster, 1 if working as single node.
     */
    int getSize();

    /**
     * Gets the name of the cluster node.
     *
     * @return the name, otherwise empty.
     */
    Optional<String> getName();


}
