package clustercode.impl.cluster.jgroups;

import clustercode.api.cluster.ClusterService;
import clustercode.api.cluster.messages.CancelTaskApiRequest;
import clustercode.api.domain.Activator;
import clustercode.api.domain.ActivatorContext;
import clustercode.api.domain.TranscodeTask;
import clustercode.api.event.RxEventBus;
import clustercode.api.event.messages.*;
import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.XSlf4j;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@XSlf4j
public class JgroupsClusterActivator implements Activator {

    private final ClusterService clusterService;
    private final RxEventBus eventBus;
    private final List<Disposable> handlers = new LinkedList<>();

    @Inject
    JgroupsClusterActivator(
        ClusterService jgroupsService,
        RxEventBus eventBus
    ) {
        this.clusterService = jgroupsService;
        this.eventBus = eventBus;
    }

    @Override
    public void preActivate(ActivatorContext context) {
        handlers.add(eventBus
            .listenFor(CancelTaskApiRequest.class)
            .filter(this::isLocalHost)
            .doOnNext(log::entry)
            .subscribe(r -> r.setCancelled(cancelTaskLocally())));

        handlers.add(eventBus
            .listenFor(CancelTaskApiRequest.class)
            .filter(this::isNotLocalHost)
            .doOnNext(log::entry)
            .subscribe(r -> r.setCancelled(true)));

        handlers.add(eventBus
            .listenFor(TranscodeBeginEvent.class)
            .map(TranscodeBeginEvent::getTask)
            .map(TranscodeTask::getMedia)
            .subscribe(clusterService::setTask));

        handlers.add(eventBus
            .listenFor(TranscodeFinishedEvent.class)
            .subscribe(e -> clusterService.removeTask()));

        handlers.add(eventBus
            .listenFor(MediaInClusterMessage.class)
            .subscribe(this::onMediaInClusterQuery));

    }

    @Override
    public void activate(ActivatorContext context) {
        log.debug("Activating JGroups cluster.");
        CompletableFuture.supplyAsync(() -> {
            clusterService.joinCluster();
            return 1;
        }).thenAccept(memberCount -> {
            eventBus.emit(
                ClusterConnectMessage.builder()
                                     .hostname(clusterService.getName().orElse("localhost"))
                                     .clusterSize(memberCount)
                                     .build());
        });
    }

    private void onMediaInClusterQuery(MediaInClusterMessage mediaInClusterMessage) {
        mediaInClusterMessage.setInCluster(clusterService.isQueuedInCluster(mediaInClusterMessage.getMedia()));
    }

    @Override
    public void deactivate(ActivatorContext context) {
        log.debug("Deactivating JGroups cluster.");
        handlers.forEach(Disposable::dispose);
        handlers.clear();
    }

    private boolean isNotLocalHost(CancelTaskApiRequest cancelTaskApiRequest) {
        return !isLocalHost(cancelTaskApiRequest);
    }

    private boolean isLocalHost(CancelTaskApiRequest cancelTaskApiRequest) {
        String localName = clusterService.getName().orElse("");
        return localName.equals(cancelTaskApiRequest.getHostname());
    }

    private boolean cancelTaskLocally() {
        return eventBus.emit(new CancelTranscodeMessage()).isCancelled();
    }

}
