package net.chrigel.clustercode.cluster;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import net.chrigel.clustercode.cluster.messages.ClusterTaskCollectionChanged;
import net.chrigel.clustercode.scan.Media;

import java.util.List;

public interface JGroupsTaskState extends JGroupsForkService {

    void setTask(Media candidate);

    void setProgress(double percentage);

    ClusterTask getCurrentTask();

    boolean isQueuedInCluster(Media candidate);

    void removeTask();

    void removeOrphanTasks();

    Flowable<ClusterTaskCollectionChanged> clusterTaskCollectionChanged();
}
