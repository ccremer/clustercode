package clustercode.impl.cluster.jgroups;

import clustercode.api.cluster.ClusterService;
import clustercode.api.cluster.JGroupsTaskState;
import clustercode.api.cluster.messages.CancelTaskRpcRequest;
import clustercode.api.domain.Media;
import io.reactivex.Flowable;
import lombok.Synchronized;

import javax.inject.Inject;
import java.util.Optional;

public class JGroupsClusterFacade implements ClusterService {

    private final SingleNodeClusterImpl singleNodeCluster;
    private final JgroupsClusterImpl jgroupsCluster;
    private ClusterService current;

    @Inject
    JGroupsClusterFacade(
            SingleNodeClusterImpl singleNodeCluster,
            JgroupsClusterImpl jgroupsCluster
    ) {
        this.singleNodeCluster = singleNodeCluster;
        this.jgroupsCluster = jgroupsCluster;
    }

    @Synchronized
    @Override
    public void joinCluster() {
        if (current == null) {
            jgroupsCluster.joinCluster();
            if (jgroupsCluster.isConnected()) {
                current = jgroupsCluster;
            } else {
                singleNodeCluster.joinCluster();
                current = singleNodeCluster;
            }
        }
    }

    @Override
    public void leaveCluster() {
        if (current != null) current.leaveCluster();
    }

    @Override
    public JGroupsTaskState getTaskState() {
        return current.getTaskState();
    }

    @Override
    public void removeTask() {
        current.removeTask();
    }

    @Override
    public boolean cancelTask(String hostname) {
        return false;
    }

    @Override
    public void setTask(Media candidate) {
        current.setTask(candidate);
    }

    @Override
    public void setProgress(double percentage) {
        current.setProgress(percentage);
    }

    @Override
    public boolean isQueuedInCluster(Media candidate) {
        return current.isQueuedInCluster(candidate);
    }

    @Override
    public int getSize() {
        return current.getSize();
    }

    @Override
    public Optional<String> getName() {
        return current.getName();
    }

    @Override
    public Flowable<CancelTaskRpcRequest> onCancelTaskRequested() {
        return current.onCancelTaskRequested();
    }
}
