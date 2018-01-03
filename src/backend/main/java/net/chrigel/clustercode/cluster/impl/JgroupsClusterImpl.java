package net.chrigel.clustercode.cluster.impl;

import io.reactivex.Flowable;
import lombok.Synchronized;
import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.cluster.ClusterService;
import net.chrigel.clustercode.cluster.JGroupsMessageDispatcher;
import net.chrigel.clustercode.cluster.JGroupsTaskState;
import net.chrigel.clustercode.cluster.messages.CancelTaskMessage;
import net.chrigel.clustercode.scan.Media;
import org.jgroups.JChannel;
import org.jgroups.fork.ForkChannel;
import org.slf4j.ext.XLogger;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@XSlf4j
class JgroupsClusterImpl
    implements ClusterService {

    private final JgroupsClusterSettings settings;
    private final JGroupsMessageDispatcher messageDispatcher;
    private final JGroupsTaskState taskState;
    private JChannel channel;
    private ScheduledExecutorService executor;

    @Inject
    JgroupsClusterImpl(JgroupsClusterSettings settings,
                       JGroupsMessageDispatcher messageDispatcher,
                       JGroupsTaskState taskState) {
        this.settings = settings;
        this.messageDispatcher = messageDispatcher;
        this.taskState = taskState;
    }

    @Synchronized
    @Override
    public void joinCluster() {
        if (isConnected()) {
            log.info("Already joined the cluster {}.", settings.getClusterName());
            return;
        }
        try {
            log.debug("Joining cluster {}...", settings.getClusterName());
            this.channel = new JChannel(settings.getJgroupsConfigFile());
            channel.setName(settings.getHostname());
            channel.connect(settings.getClusterName());

            ForkChannel taskChannel = new ForkChannel(channel, "tasks", "tasks_ch");
            taskChannel.connect(settings.getClusterName());
            taskState.initialize(taskChannel, channel.getAddressAsString());

            ForkChannel rpcChannel = new ForkChannel(channel, "rpc", "rpc_ch");
            rpcChannel.connect(settings.getClusterName());
            messageDispatcher.initialize(rpcChannel, channel.getAddressAsString());

            if (executor != null) executor.shutdown();
            executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(taskState::removeOrphanTasks, 1, 1, TimeUnit.MINUTES);

            log.info("Joined cluster {} with {} member(s).",
                channel.getClusterName(), channel.getView().getMembers().size());
            log.info("Cluster address: {}", channel.getAddress());
        } catch (Exception e) {
            channel = null;
            log.catching(XLogger.Level.WARN, e);
            log.info("Could not create or join cluster. Will work as single node.");
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
        if (!isConnected()) return;
        taskState.setTask(candidate);
    }

    public void setProgress(double percentage) {
        if (!isConnected()) return;
        taskState.setProgress(percentage);
    }

    @Override
    public boolean isQueuedInCluster(Media candidate) {
        return isConnected() && taskState.isQueuedInCluster(candidate);
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
    public Flowable<CancelTaskMessage> onCancelTaskRequested() {
        return null;
    }

}
