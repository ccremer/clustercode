package clustercode.api.rest.v1.hook;

import com.google.inject.Inject;
import lombok.Synchronized;
import lombok.extern.slf4j.XSlf4j;
import clustercode.api.cluster.ClusterTask;
import clustercode.api.cluster.messages.CancelTaskApiRequest;
import clustercode.api.cluster.messages.ClusterTaskCollectionChanged;
import clustercode.api.event.RxEventBus;

import java.util.Collection;
import java.util.Collections;

@XSlf4j
public class TaskHookImpl implements TaskHook {

    private final RxEventBus eventBus;
    private Collection<ClusterTask> clusterTasks = Collections.emptyList();

    @Inject
    TaskHookImpl(RxEventBus eventBus) {
        this.eventBus = eventBus;
        eventBus.listenFor(ClusterTaskCollectionChanged.class)
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
    public boolean cancelTask() {
        return eventBus.emit(new CancelTaskApiRequest("", false)).isCancelled();
    }

}
