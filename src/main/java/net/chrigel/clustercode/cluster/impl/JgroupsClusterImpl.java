package net.chrigel.clustercode.cluster.impl;

import lombok.Synchronized;
import lombok.extern.slf4j.XSlf4j;
import lombok.val;
import net.chrigel.clustercode.cluster.ClusterService;
import net.chrigel.clustercode.cluster.ClusterTask;
import net.chrigel.clustercode.cluster.JGroupsMessageDispatcher;
import net.chrigel.clustercode.scan.Media;
import org.jgroups.JChannel;
import org.jgroups.blocks.ReplicatedHashMap;
import org.jgroups.fork.ForkChannel;
import org.slf4j.ext.XLogger;

import javax.inject.Inject;
import java.io.File;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@XSlf4j
class JgroupsClusterImpl
    implements ClusterService {

    private final JgroupsClusterSettings settings;
    private final Clock clock;
    private final JGroupsMessageDispatcher messageDispatcher;
    private ReplicatedHashMap<String, ClusterTask> map;
    private JChannel channel;
    private ScheduledExecutorService executor;

    @Inject
    JgroupsClusterImpl(JgroupsClusterSettings settings,
                       Clock clock,
                       JGroupsMessageDispatcher messageDispatcher) {
        this.settings = settings;
        this.clock = clock;
        this.messageDispatcher = messageDispatcher;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
                Thread thread = new Thread();
                thread.setDaemon(true);
                thread.setName("shutdown-hook-thread");
                return thread;
            });
            Future<?> future = executor.submit(this::leaveCluster);
            try {
                future.get(5, TimeUnit.SECONDS);
            } catch (TimeoutException | ExecutionException | InterruptedException e) {
                future.cancel(true);
                log.catching(e);
            }
            executor.shutdownNow();
        }));
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
            this.map = new ReplicatedHashMap<>(channel);
            map.setBlockingUpdates(true);
            if (executor != null) executor.shutdown();
            executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(this::removeOrphanTasks, 0, 1, TimeUnit.HOURS);
            map.start(5000L);
            map.remove(getChannelAddress());

            ForkChannel forkChannel = new ForkChannel(channel, "rpc", "rpc_ch");

            messageDispatcher.initialize(forkChannel);
            log.info("Joined cluster {} with {} member(s).",
                channel.getClusterName(), channel.getView().getMembers().size());
            log.info("Cluster address: {}", channel.getAddress());
        } catch (Exception e) {
            channel = null;
            map = null;
            log.catching(XLogger.Level.WARN, e);
            log.info("Could not create or join cluster. Will work as single node.");
        }
    }

    private boolean isConnected() {
        return channel != null && channel.isConnected() && map != null;
    }

    private String getChannelAddress() {
        if (!isConnected()) return null;
        return channel.getAddressAsUUID();
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

    ZonedDateTime getCurrentUtcTime() {
        return ZonedDateTime.ofInstant(Instant.now(clock), ZoneOffset.UTC);
    }

    ClusterTask createTaskFor(Media candidate) {
        val currentTime = getCurrentUtcTime();
        return ClusterTask.builder()
            .priority(candidate.getPriority())
            .sourceName(toLinuxPath(candidate.getSourcePath().toString()))
            .dateAdded(currentTime)
            .lastUpdated(currentTime)
            .memberName(getName().isPresent() ? getName().get() : "")
            .build();
    }

    private String toLinuxPath(String path) {
        return path.replace("\\", "/");
    }

    private void removeOrphanTasks() {
        if (!isConnected()) return;
        ZonedDateTime current = getCurrentUtcTime();
        log.debug("Removing orphan tasks.");
        map.entrySet()
            .removeIf(entry -> entry
                .getValue()
                .getDateAdded()
                .plusHours(settings.getTaskTimeout())
                .isBefore(current));
    }

    @Synchronized
    @Override
    public void removeTask() {
        if (!isConnected()) return;
        log.debug("Removing current task.");
        map.remove(getChannelAddress());
    }

    @Override
    public boolean cancelTask(String hostname) {
        if (hostname == null) hostname = channel.getAddressAsString();
        return messageDispatcher.cancelTask(hostname);
    }

    @Override
    public List<ClusterTask> getTasks() {
        if (!isConnected()) return Collections.emptyList();

        return new ArrayList<>(map.values());
    }

    @Override
    public void setTask(Media candidate) {
        if (!isConnected()) return;
        ClusterTask clusterTask = createTaskFor(candidate);
        updateTask(clusterTask);
    }

    @Override
    public void setProgress(double percentage) {
        if (!isConnected()) return;
        val task = getCurrentTask();
        if (task == null) return;
        task.setPercentage(percentage);
        updateTask(task);
    }

    @Synchronized
    private void updateTask(ClusterTask clusterTask) {
        String address = getChannelAddress();
        if (map.containsKey(address)) {
            log.debug("Updating media state.");
            clusterTask.setLastUpdated(getCurrentUtcTime());
            map.replace(address, clusterTask);
        } else {
            map.put(address, clusterTask);
            log.info("{} accepted in cluster.", clusterTask);
        }
    }

    private ClusterTask getCurrentTask() {
        if (!isConnected()) return null;
        return map.get(getChannelAddress());
    }

    boolean isFileEquals(String first, String other) {
        return new File(toLinuxPath(first)).equals(new File(toLinuxPath(other)));
    }

    @Override
    public boolean isQueuedInCluster(Media candidate) {
        if (!isConnected()) return false;
        log.debug("Testing whether {} is in cluster", candidate.getSourcePath().toString());

        boolean found = map.values().stream()
            .anyMatch(clusterTask -> isFileEquals(
                candidate.getSourcePath().toString(), clusterTask.getSourceName()));
        if (found) log.debug("{} is already in cluster.", candidate);
        return found;
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

}
