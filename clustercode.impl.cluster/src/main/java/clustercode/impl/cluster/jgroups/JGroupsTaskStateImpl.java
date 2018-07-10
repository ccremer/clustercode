package clustercode.impl.cluster;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import lombok.extern.slf4j.XSlf4j;
import lombok.val;
import clustercode.api.cluster.ClusterTask;
import clustercode.api.cluster.JGroupsTaskState;
import clustercode.api.cluster.messages.ClusterTaskCollectionChanged;
import clustercode.api.domain.Media;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.View;
import org.jgroups.blocks.ReplicatedHashMap;

import javax.inject.Inject;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@XSlf4j
public class JGroupsTaskStateImpl implements JGroupsTaskState {

    private final Clock clock;
    private final Subject<ClusterTaskCollectionChanged> clusterTaskCollectionChanged;
    private ReplicatedHashMap<String, ClusterTask> map;
    private String hostname;

    @Inject
    JGroupsTaskStateImpl(Clock clock) {
        this.clock = clock;
        Subject<ClusterTaskCollectionChanged> subject = PublishSubject.create();
        this.clusterTaskCollectionChanged = subject.toSerialized();
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
    public Flowable<ClusterTaskCollectionChanged> clusterTaskCollectionChanged() {
        return clusterTaskCollectionChanged
            .toFlowable(BackpressureStrategy.BUFFER)
            .observeOn(Schedulers.computation());
    }

    @Override
    public void initialize(JChannel channel, String hostname) throws Exception {
        this.map = new ReplicatedHashMap<>(channel);
        this.hostname = hostname;
        map.setBlockingUpdates(true);
        map.start(5000L);
        map.remove(getChannelAddress());

        map.addNotifier(new ObservableNotifier());
    }

    private String getChannelAddress() {
        return map.getChannel().getAddressAsUUID();
    }

    class ObservableNotifier implements ReplicatedHashMap.Notification<String, ClusterTask> {

        @Override
        public void entrySet(String key, ClusterTask value) {
            log.entry(key, value);
            clusterTaskCollectionChanged.onNext(ClusterTaskCollectionChanged
                .builder()
                .tasks(map.values())
                .build());
        }

        @Override
        public void entryRemoved(String key) {
            log.entry(key);
            clusterTaskCollectionChanged.onNext(ClusterTaskCollectionChanged
                .builder()
                .removed(true)
                .tasks(map.values())
                .build());
        }

        @Override
        public void viewChange(View view, List<Address> mbrs_joined, List<Address> mbrs_left) {

        }

        @Override
        public void contentsSet(Map<String, ClusterTask> new_entries) {
            log.entry(new_entries);
            clusterTaskCollectionChanged.onNext(ClusterTaskCollectionChanged
                .builder()
                .added(new_entries.values())
                .tasks(map.values())
                .build());
        }

        @Override
        public void contentsCleared() {
            log.entry();
            clusterTaskCollectionChanged.onNext(ClusterTaskCollectionChanged
                .builder()
                .cleared(true)
                .tasks(map.values())
                .build());
        }
    }
}
