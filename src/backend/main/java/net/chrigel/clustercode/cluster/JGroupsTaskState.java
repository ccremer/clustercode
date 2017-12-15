package net.chrigel.clustercode.cluster;

import net.chrigel.clustercode.scan.Media;

import java.util.List;

public interface JGroupsTaskState extends JGroupsForkService {
    List<ClusterTask> getTasks();
    void setTask(Media candidate);
    void setProgress(double percentage);
    ClusterTask getCurrentTask();
    boolean isQueuedInCluster(Media candidate);
    void removeTask();
    void removeOrphanTasks();
}
