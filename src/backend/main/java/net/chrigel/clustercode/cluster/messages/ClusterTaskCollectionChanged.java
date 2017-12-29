package net.chrigel.clustercode.cluster.messages;

import lombok.*;
import net.chrigel.clustercode.cluster.ClusterTask;

import java.util.Collection;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class ClusterTaskCollectionChanged {

    private Collection<ClusterTask> clusterTasksAdded;

    private boolean removed;

    private boolean cleared;

}
