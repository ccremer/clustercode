package clustercode.api.cluster;

import io.reactivex.Flowable;
import clustercode.api.cluster.messages.CancelTaskRpcRequest;
import clustercode.api.domain.Media;

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

    JGroupsTaskState getTaskState();

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
     * Sets the cleanup which is being executed by this Java process. Replaces the old cleanup if present, only one task
     * can be active.
     * This method does nothing if not connected to the cluster.
     *
     * @param candidate the candidate, not null.
     */
    void setTask(Media candidate);

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

    Flowable<CancelTaskRpcRequest> onCancelTaskRequested();

}
