package clustercode.impl.cluster.jgroups;

import clustercode.api.cluster.ClusterService;
import clustercode.api.domain.Media;
import lombok.Synchronized;
import lombok.extern.slf4j.XSlf4j;

import java.util.Optional;

@XSlf4j
public class JgroupsClusterImpl
        implements ClusterService {

    private Media current;

    @Synchronized
    @Override
    public void joinCluster() {
        log.warn("This is not a jgroups cluster anymore, doing nothing");
    }

    @Synchronized
    @Override
    public void removeTask() {
        current = null;
    }

    @Override
    public void setTask(Media candidate) {
        // TODO: add to messaging
        current = candidate;
    }

    @Override
    public boolean isQueuedInCluster(Media candidate) {
        return candidate.equals(current);
    }

    @Override
    public Optional<String> getName() {
        return Optional.empty();
    }

}
