package net.chrigel.clustercode.cluster.impl;

import com.google.inject.Inject;
import lombok.extern.slf4j.XSlf4j;
import lombok.val;
import net.chrigel.clustercode.cluster.ClusterTask;
import net.chrigel.clustercode.cluster.JGroupsTaskState;
import net.chrigel.clustercode.cluster.messages.ClusterTaskCollectionChanged;
import net.chrigel.clustercode.event.RxEventBus;
import net.chrigel.clustercode.scan.Media;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.View;
import org.jgroups.blocks.ReplicatedHashMap;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@XSlf4j
public class JGroupsTaskStateImpl implements JGroupsTaskState {

    private final Clock clock;
    private final RxEventBus eventBus;
    private ReplicatedHashMap<String, ClusterTask> map;
    private String hostname;

    @Inject
    JGroupsTaskStateImpl(Clock clock,
                         RxEventBus eventBus) {
        this.clock = clock;
        this.eventBus = eventBus;
    }

    @Override
    public List<ClusterTask> getTasks() {
        return new ArrayList<>(map.values());
    }

    @Override
    public void setTask(Media candidate) {
        ClusterTask clusterTask = createTaskFor(candidate);
        updateTask(clusterTask);
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
                          .memberName(hostname)
                          .build();
    }

    private String toLinuxPath(String path) {
        return path.replace("\\", "/");
    }


    @Override
    public void setProgress(double percentage) {
        val task = getCurrentTask();
        if (task == null) return;
        task.setPercentage(percentage);
        updateTask(task);
    }

    private void updateTask(ClusterTask clusterTask) {
        String address = getChannelAddress();
        if (map.containsKey(address)) {
            log.debug("Updating media state.");
            clusterTask.setLastUpdated(getCurrentUtcTime());
            /*
            We don't use replace method. If another node does not have the key, it won't get added.
            So we remove it, if it exists and re-add (just to be sure, the docs don't specify whether replace adds if
             missing).
            map.replace(address, clusterTask);
             */
            map.remove(address);
            map.put(address, clusterTask);
        } else {
            map.put(address, clusterTask);
            log.info("{} accepted in cluster.", clusterTask);
        }
    }

    @Override
    public ClusterTask getCurrentTask() {
        return map.get(getChannelAddress());
    }

    @Override
    public boolean isQueuedInCluster(Media candidate) {
        log.debug("Testing whether {} is in cluster", toLinuxPath(candidate.getSourcePath().toString()));
        boolean found = map.values().stream()
                           .anyMatch(clusterTask -> fileEquals(clusterTask.getSourceName(), candidate.getSourcePath()));
        if (found) log.debug("{} is already in cluster.", candidate);
        return found;
    }

    boolean fileEquals(String first, Path second) {
        return toLinuxPath(first).equalsIgnoreCase(toLinuxPath(second.toString()));
    }

    @Override
    public void removeTask() {
        log.debug("Removing current task.");
        map.remove(getChannelAddress());
    }

    @Override
    public void removeOrphanTasks() {
        ZonedDateTime current = getCurrentUtcTime();
        log.debug("Removing orphan tasks.");
        map.entrySet()
           .removeIf(entry -> entry
               .getValue()
               .getLastUpdated()
               .plusMinutes(1)
               .isBefore(current));
    }

    @Override
    public void initialize(JChannel channel, String hostname) throws Exception {
        this.map = new ReplicatedHashMap<>(channel);
        this.hostname = hostname;
        map.setBlockingUpdates(true);
        map.start(5000L);
        map.remove(getChannelAddress());

        map.addNotifier(createNotifier());
    }

    private String getChannelAddress() {
        return map.getChannel().getAddressAsUUID();
    }

    private ReplicatedHashMap.Notification<String, ClusterTask> createNotifier() {
        return new ReplicatedHashMap.Notification<String, ClusterTask>() {
            @Override
            public void entrySet(String key, ClusterTask value) {
                eventBus.emit(ClusterTaskCollectionChanged
                    .builder()
                    .clusterTasksAdded(Collections.singletonList(value))
                    .build());
            }

            @Override
            public void entryRemoved(String key) {
                eventBus.emit(ClusterTaskCollectionChanged
                    .builder()
                    .removed(true)
                    .build());
            }

            @Override
            public void viewChange(View view, List<Address> mbrs_joined, List<Address> mbrs_left) {

            }

            @Override
            public void contentsSet(Map<String, ClusterTask> new_entries) {
                eventBus.emit(ClusterTaskCollectionChanged
                    .builder()
                    .clusterTasksAdded(new_entries.values())
                    .build());
            }

            @Override
            public void contentsCleared() {
                eventBus.emit(ClusterTaskCollectionChanged
                    .builder()
                    .cleared(true)
                    .build());
            }
        };
    }
}
