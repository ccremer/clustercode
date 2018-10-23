package clustercode.impl.cluster.jgroups;

import clustercode.api.cluster.ClusterService;
import clustercode.api.cluster.JGroupsMessageDispatcher;
import clustercode.api.cluster.JGroupsTaskState;
import clustercode.api.cluster.messages.CancelTaskRpcRequest;
import clustercode.api.domain.Media;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import lombok.Synchronized;
import lombok.extern.slf4j.XSlf4j;
import org.jgroups.JChannel;
import org.jgroups.fork.ForkChannel;
import org.slf4j.ext.XLogger;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@XSlf4j
public class JgroupsClusterImpl
        implements ClusterService {

    private final JgroupsClusterConfig config;
    private final JGroupsMessageDispatcher messageDispatcher;
    private final JGroupsTaskState taskState;
    private final SingleNodeClusterImpl singleNodeCluster;
    private JChannel channel;
    private ScheduledExecutorService executor;

    @Inject
    JgroupsClusterImpl(JgroupsClusterConfig config,
                       JGroupsMessageDispatcher messageDispatcher,
                       JGroupsTaskState taskState,
                       SingleNodeClusterImpl singleNodeCluster
    ) {
        this.config = config;
        this.messageDispatcher = messageDispatcher;
        this.taskState = taskState;
        this.singleNodeCluster = singleNodeCluster;
    }

    @Synchronized
    @Override
    public void joinCluster() throws Exception {
        if (isConnected()) {
            log.info("Already joined the cluster {}.", config.cluster_name());
            return;
        }
        try {
            log.debug("Joining cluster {}...", config.cluster_name());
            this.channel = new JChannel(config.jgroups_config_file());
            channel.setName(config.hostname());
            channel.connect(config.cluster_name());

            ForkChannel taskChannel = new ForkChannel(channel, "tasks", "tasks_ch");
            taskChannel.connect(config.cluster_name());
            taskState.initialize(taskChannel, channel.getAddressAsString());

            ForkChannel rpcChannel = new ForkChannel(channel, "rpc", "rpc_ch");
            rpcChannel.connect(config.cluster_name());
            messageDispatcher.initialize(rpcChannel, channel.getAddressAsString());

            if (executor != null) executor.shutdown();
            executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(taskState::removeOrphanTasks, 1, 1, TimeUnit.MINUTES);

            log.info("Joined cluster {} with {} member(s).",
                    channel.getClusterName(), channel.getView().getMembers().size());
            log.info("Cluster address: {}", channel.getAddress());
        } catch (Exception e) {
            channel = null;
            throw new Exception("Could not create or join cluster", e);
        }
    }

    private boolean isConnected() {
        return channel != null && channel.isConnected();
    }

    @Synchronized
    @Override
    public void leaveCluster() {
        if (channel == null) return;
        try {
            log.info("Leaving cluster...");
            channel.disconnect();
            if (executor != null) executor.shutdown();
            log.info("Left the cluster.");
        } catch (Exception ex) {
            log.catching(XLogger.Level.WARN, ex);
        }
        channel = null;
    }

    @Override
    public JGroupsTaskState getTaskState() {
        return taskState;
    }

    @Synchronized
    @Override
    public void removeTask() {
        if (!isConnected()) return;
        taskState.removeTask();
    }

    @Override
    public boolean cancelTask(String hostname) {
        if (hostname == null) hostname = channel.getAddressAsString();
        return messageDispatcher.cancelTask(hostname);
    }

    @Override
    public void setTask(Media candidate) {
        if (isConnected()) taskState.setTask(candidate);
        singleNodeCluster.setTask(candidate);
    }

    @Override
    public void setProgress(double percentage) {
        if (!isConnected()) return;
        taskState.setProgress(percentage);
    }

    @Override
    public boolean isQueuedInCluster(Media candidate) {
        if (isConnected()) return taskState.isQueuedInCluster(candidate);
        return false;
    }

    @Override
    public int getSize() {
        if (!isConnected()) return 0;
        return channel.getView().size();
    }

    @Override
    public Optional<String> getName() {
        if (!isConnected()) return Optional.empty();
        return Optional.ofNullable(channel.getAddressAsString());
    }

    @Override
    public Flowable<CancelTaskRpcRequest> onCancelTaskRequested() {
        return messageDispatcher
                .onRpcTaskCancelled()
                .doOnNext(log::entry)
                .toFlowable(BackpressureStrategy.BUFFER);
    }

}
