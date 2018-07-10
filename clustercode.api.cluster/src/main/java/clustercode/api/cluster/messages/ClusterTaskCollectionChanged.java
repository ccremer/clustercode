package clustercode.api.cluster.messages;

import clustercode.api.cluster.ClusterTask;
import lombok.*;

import java.util.Collection;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class ClusterTaskCollectionChanged {

    @Singular("added")
    private Collection<ClusterTask> added;

    /**
     * Gets the tasks that are scheduled in the cluster.
     */
    @Singular("tasks")
    private Collection<ClusterTask> tasks;

    private boolean removed;

    private boolean cleared;

}
