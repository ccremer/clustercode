package clustercode.api.cluster;

import io.reactivex.Flowable;
import clustercode.api.cluster.messages.ClusterTaskCollectionChanged;
import clustercode.api.domain.Media;

public interface JGroupsTaskState extends JGroupsForkService {

    void setTask(Media candidate);

    void setProgress(double percentage);

    ClusterTask getCurrentTask();

    boolean isQueuedInCluster(Media candidate);

    void removeTask();

    void removeOrphanTasks();

    Flowable<ClusterTaskCollectionChanged> clusterTaskCollectionChanged();
}
