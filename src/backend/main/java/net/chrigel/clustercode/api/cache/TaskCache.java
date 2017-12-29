package net.chrigel.clustercode.api.cache;

import com.google.inject.Inject;
import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.cluster.ClusterService;
import net.chrigel.clustercode.cluster.ClusterTask;
import net.chrigel.clustercode.cluster.messages.ClusterTaskCollectionChanged;
import net.chrigel.clustercode.event.RxEventBus;

import java.util.Collection;
import java.util.Collections;

@XSlf4j
public class TaskCache {

    private final ClusterService clusterService;

    @Inject
    TaskCache(RxEventBus eventBus,
              ClusterService clusterService){
        this.clusterService = clusterService;
        eventBus.register(ClusterTaskCollectionChanged.class, this::onTaskCollectionChanged);
    }

    private void onTaskCollectionChanged(ClusterTaskCollectionChanged event) {
        log.debug("Task collection changed: {}", event);
        // for future web sockets usage.
    }

    public Collection<ClusterTask> getClusterTasks() {
        return Collections.unmodifiableCollection(clusterService.getTasks());
    }

}
