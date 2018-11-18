package clustercode.impl.cluster.jgroups;

import clustercode.api.cluster.ClusterService;
import clustercode.api.domain.Media;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class SingleNodeClusterImpl implements ClusterService {

    private Media task;

    @Override
    public void joinCluster() {
        log.info("Will work as a single-node cluster.");
    }

    @Override
    public void removeTask() {
        this.task = null;
    }

    @Override
    public void setTask(Media candidate) {
        this.task = candidate;
    }

    @Override
    public boolean isQueuedInCluster(Media candidate) {
        if (candidate == null) return false;
        return candidate.equals(task);
    }

    @Override
    public Optional<String> getName() {
        return Optional.of("localhost");
    }

}
