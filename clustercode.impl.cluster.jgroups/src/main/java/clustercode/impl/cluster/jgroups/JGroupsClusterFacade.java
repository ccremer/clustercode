package clustercode.impl.cluster.jgroups;

import clustercode.api.cluster.ClusterService;
import clustercode.api.domain.Media;
import lombok.Synchronized;

import javax.inject.Inject;
import java.util.Optional;

public class JGroupsClusterFacade implements ClusterService {

    private final JgroupsClusterImpl jgroupsCluster;
    private ClusterService current;

    @Inject
    JGroupsClusterFacade(
        JgroupsClusterImpl jgroupsCluster
    ) {
        this.jgroupsCluster = jgroupsCluster;
    }

    @Synchronized
    @Override
    public void joinCluster() {
        if (current == null) {
            jgroupsCluster.joinCluster();
            current = jgroupsCluster;
        }
    }

    @Override
    public void removeTask() {
        current.removeTask();
    }

    @Override
    public void setTask(Media candidate) {
        current.setTask(candidate);
    }

    @Override
    public boolean isQueuedInCluster(Media candidate) {
        return current.isQueuedInCluster(candidate);
    }

    @Override
    public Optional<String> getName() {
        return current.getName();
    }

}
