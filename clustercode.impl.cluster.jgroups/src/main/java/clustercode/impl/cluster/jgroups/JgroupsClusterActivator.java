package clustercode.impl.cluster.jgroups;

import clustercode.api.cluster.ClusterService;
import clustercode.api.cluster.messages.CancelTaskApiRequest;
import clustercode.api.domain.Activator;
import clustercode.api.domain.ActivatorContext;
import clustercode.api.domain.TranscodeTask;
import clustercode.api.event.RxEventBus;
import clustercode.api.event.messages.*;
import clustercode.api.transcode.TranscodeReport;
import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.XSlf4j;
import org.slf4j.ext.XLogger;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@XSlf4j
public class JgroupsClusterActivator implements Activator {

    private final ClusterService clusterService;
    private final JgroupsClusterConfig config;
    private final RxEventBus eventBus;
    private final List<Disposable> handlers = new LinkedList<>();

    @Inject
    JgroupsClusterActivator(
            ClusterService jgroupsService,
            JgroupsClusterConfig config,
            RxEventBus eventBus
    ) {
        this.clusterService = jgroupsService;
        this.config = config;
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
                .subscribe(r -> r.setCancelled(clusterService.cancelTask(r.getHostname()))));

        handlers.add(eventBus
                .listenFor(TranscodeReport.class)
                .sample(10, TimeUnit.SECONDS)
                .map(TranscodeReport::getPercentage)
                .subscribe(clusterService::setProgress));

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
            return clusterService.getSize();
        }).thenAccept(memberCount -> {
            eventBus.emit(
                    ClusterConnectMessage.builder()
                                         .hostname(clusterService.getName().orElse("localhost"))
                                         .arbiterNode(config.arbiter_enabled())
                                         .clusterSize(memberCount)
                                         .build());

            clusterService.getTaskState()
                          .clusterTaskCollectionChanged()
                          .doOnNext(c -> log.warn("{}", c))
                          .subscribe(eventBus::emit);

            clusterService.onCancelTaskRequested()
                          .subscribe(r -> r.setCancelled(cancelTaskLocally()));
        });
    }

    private void onMediaInClusterQuery(MediaInClusterMessage mediaInClusterMessage) {
        mediaInClusterMessage.setInCluster(clusterService.isQueuedInCluster(mediaInClusterMessage.getMedia()));
    }

    @Override
    public void deactivate(ActivatorContext context) {
        log.debug("Deactivating JGroups cluster.");
        handlers.forEach(Disposable::dispose);
        clusterService.leaveCluster();
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
