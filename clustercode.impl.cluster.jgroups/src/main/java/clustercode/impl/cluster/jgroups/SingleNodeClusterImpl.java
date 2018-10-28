package clustercode.impl.cluster.jgroups;

import clustercode.api.cluster.ClusterService;
import clustercode.api.cluster.JGroupsTaskState;
import clustercode.api.cluster.messages.CancelTaskRpcRequest;
import clustercode.api.domain.Media;
import io.reactivex.Flowable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.Optional;

@Slf4j
public class SingleNodeClusterImpl implements ClusterService {

    private final Subject<Object> publishSubject;
    private Media task;

    @Inject
    SingleNodeClusterImpl() {
        this.publishSubject = PublishSubject.create().toSerialized();
    }

    @Override
    public void joinCluster() {
        log.info("Will work as a single-node cluster.");
    }

    @Override
    public void leaveCluster() {
        log.debug("Not leaving any cluster, this is a no-op.");
    }

    @Override
    public JGroupsTaskState getTaskState() {
        return null;
    }

    @Override
    public void removeTask() {
        this.task = null;
    }

    @Override
    public boolean cancelTask(String hostname) {
        return true;
    }

    @Override
    public void setTask(Media candidate) {
        this.task = candidate;
    }

    @Override
    public void setProgress(double percentage) {

    }

    @Override
    public boolean isQueuedInCluster(Media candidate) {
        if (candidate == null) return false;
        return candidate.equals(task);
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public Optional<String> getName() {
        return Optional.of("localhost");
    }

    @Override
    public Flowable<CancelTaskRpcRequest> onCancelTaskRequested() {
        return Flowable.empty();
    }
}
