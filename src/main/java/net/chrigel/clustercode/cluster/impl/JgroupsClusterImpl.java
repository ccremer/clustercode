package net.chrigel.clustercode.cluster.impl;

import lombok.Synchronized;
import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.cluster.ClusterService;
import net.chrigel.clustercode.scan.Media;
import net.chrigel.clustercode.util.FilesystemProvider;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.blocks.ReplicatedHashMap;
import org.slf4j.ext.XLogger;

import javax.inject.Inject;
import java.io.File;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@XSlf4j
class JgroupsClusterImpl implements ClusterService {

    private final JgroupsClusterSettings settings;
    private final Clock clock;
    private Optional<ReplicatedHashMap<Address, ClusterItem>> map = Optional.empty();
    private Optional<JChannel> channel = Optional.empty();
    private Optional<ScheduledExecutorService> executor = Optional.empty();

    @Inject
    JgroupsClusterImpl(JgroupsClusterSettings settings,
                       Clock clock) {
        this.settings = settings;
        this.clock = clock;
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
                leaveCluster()
        ));
    }

    @Synchronized
    @Override
    public void joinCluster() {

        if (channel.isPresent() && channel.get().isConnected()) {
            log.info("Already joined the cluster {}.", settings.getClusterName());
        } else {
            try {
                log.debug("Joining cluster {}...", settings.getClusterName());
                JChannel ch = new JChannel(settings.getJgroupsConfigFile());
                ch.setName(settings.getHostname());
                ch.connect(settings.getClusterName());
                ReplicatedHashMap<Address, ClusterItem> replicatedMap = new ReplicatedHashMap<>(ch);
                executor.ifPresent(executor -> executor.shutdown());
                executor = Optional.of(Executors.newSingleThreadScheduledExecutor());
                executor.get().scheduleAtFixedRate(() ->
                                removeOrphanTasks(),
                        0, 1, TimeUnit.HOURS);
                replicatedMap.start(5000L);
                replicatedMap.remove(ch.getAddress());
                this.map = Optional.of(replicatedMap);
                this.channel = Optional.of(ch);
                log.info("Joined cluster {} with {} member(s).",
                        ch.getClusterName(), ch.getView().getMembers().size());
                log.info("Cluster address: {}", ch.getAddress());
            } catch (Exception e) {
                log.catching(XLogger.Level.WARN, e);
                log.info("Could not create or join cluster. Will work as single node.");
            }
        }
    }

    @Synchronized
    @Override
    public void leaveCluster() {
        channel.ifPresent(ch -> {
                    try {
                        log.debug("Leaving cluster...");
                        ch.close();
                        executor.ifPresent(executor -> executor.shutdown());
                        log.info("Left the cluster.");
                    } catch (Exception ex) {
                        log.catching(XLogger.Level.WARN, ex);
                    }
                }
        );
        channel = Optional.empty();
    }

    ZonedDateTime getCurrentUtcTime() {
        return ZonedDateTime.ofInstant(Instant.now(clock), ZoneOffset.UTC);
    }

    ClusterItem createTaskFor(Media candidate) {
        return ClusterItem.builder()
                .priority(candidate.getPriority())
                .sourceName(candidate.getSourcePath().toFile().toString())
                .dateAdded(getCurrentUtcTime())
                .build();
    }

    Media createCandidateFor(ClusterItem clusterItem) {
        return Media.builder()
                .priority(clusterItem.getPriority())
                .sourcePath(FilesystemProvider.getInstance().getPath(clusterItem.getSourceName()))
                .build();
    }

    private void removeOrphanTasks() {
        map.ifPresent(map -> {
            ZonedDateTime current = getCurrentUtcTime();
            map.entrySet()
                    .removeIf(entry -> entry
                            .getValue()
                            .getDateAdded()
                            .plusHours(24)
                            .isBefore(current));
        });
    }

    @Synchronized
    @Override
    public void removeTask() {
        if (map.isPresent() && channel.isPresent()) {
            map.get().remove(channel.get().getAddress());
        }
    }

    @Override
    public Optional<Media> getTask() {
        if (map.isPresent() && map.get().containsKey(channel.get().getAddress())) {
            return Optional.of(createCandidateFor(map.get().get(channel.get().getAddress())));
        } else {
            return Optional.empty();
        }

    }

    @Synchronized
    @Override
    public void setTask(Media candidate) {
        if (map.isPresent() && channel.isPresent()) {
            Address address = channel.get().getAddress();
            log.debug("Replacing clusterItem in map...");
            ClusterItem clusterItem = createTaskFor(candidate);
            if (map.get().containsKey(address)) {
                map.get().replace(address, clusterItem);
            } else {
                map.get().put(address, clusterItem);
            }
            log.debug("Waiting for clusterItem acceptance...");
            // wait until the clusterItem is in the map.
            while (!map.get().containsValue(clusterItem)) {
            }
            log.info("ClusterItem accepted in cluster.");
        }
    }

    boolean isFileEquals(String first, String other) {
        log.debug("Comparing file paths: {} : {}", first, other);
        return new File(first).equals(new File(other));
    }

    @Override
    public boolean isQueuedInCluster(Media candidate) {
        return map.isPresent() &&
                map.get().values().stream()
                        .anyMatch(clusterItem -> isFileEquals(candidate.getSourcePath().toString(), clusterItem.getSourceName()));
    }
}
