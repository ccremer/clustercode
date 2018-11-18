package clustercode.api.rest.v1.hook;

import clustercode.api.cluster.ClusterTask;

import java.util.Collection;

/**
 * Represents an interface that hooks into the lifecycle of the state machine.
 */
public interface TaskHook {

    /**
     * Gets the most recent collection of tasks that are scheduled in the cluster. The implementation listens for
     * {@link clustercode.api.cluster.messages.ClusterTaskCollectionChanged} messages. Multiple invocations
     * may return the same list, though it can be changed at arbitrary times.
     *
     * @return the collection (not null). May be empty.
     */
    Collection<ClusterTask> getClusterTasks();

    /**
     * Tries to cancel the task that is running on the given host. This method runs synchronously and may take some
     * time depending on network usage.
     *
     * @return true if a node confirmed cancellation, false if unknown or failed.
     */
    boolean cancelTask();

}
