package net.chrigel.clustercode.api.hook;

import com.google.inject.Inject;
import lombok.Synchronized;
import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.cluster.ClusterTask;
import net.chrigel.clustercode.cluster.messages.ClusterTaskCollectionChanged;
import net.chrigel.clustercode.cluster.messages.CancelTaskApiRequest;
import net.chrigel.clustercode.event.RxEventBus;

import java.util.Collection;
import java.util.Collections;

@XSlf4j
public class TaskHookImpl implements TaskHook {

    private final RxEventBus eventBus;
    private Collection<ClusterTask> clusterTasks = Collections.emptyList();

    @Inject
    TaskHookImpl(RxEventBus eventBus) {
        this.eventBus = eventBus;
        eventBus.register(ClusterTaskCollectionChanged.class)
                .subscribe(this::onTaskCollectionChanged);
    }

    @Synchronized
    private void onTaskCollectionChanged(ClusterTaskCollectionChanged event) {
        log.debug("Task collection changed: {}", event);
        this.clusterTasks = event.getTasks();
        // for future web sockets usage.
    }

    @Override
    public Collection<ClusterTask> getClusterTasks() {
        return clusterTasks;
    }

    @Override
    public boolean cancelTask(String hostname) {
        return eventBus.emit(new CancelTaskApiRequest(hostname, false)).isCancelled();
    }

}
