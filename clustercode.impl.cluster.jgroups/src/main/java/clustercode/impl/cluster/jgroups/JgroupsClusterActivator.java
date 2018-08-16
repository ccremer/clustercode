package clustercode.impl.cluster.jgroups;

import clustercode.api.cluster.messages.CancelTaskApiRequest;
import clustercode.api.domain.Activator;
import clustercode.api.domain.ActivatorContext;
import clustercode.api.event.RxEventBus;
import clustercode.api.event.messages.CancelTranscodeMessage;
import clustercode.api.event.messages.ClusterJoinedMessage;
import clustercode.api.event.messages.MediaInClusterMessage;
import clustercode.api.transcode.TranscodeProgress;
import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.XSlf4j;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@XSlf4j
public class JgroupsClusterActivator implements Activator {

    private final JgroupsClusterImpl clusterService;
    private final JgroupsClusterConfig config;
    private final RxEventBus eventBus;
    private final List<Disposable> handlers = new LinkedList<>();

    @Inject
    JgroupsClusterActivator(
            JgroupsClusterImpl clusterService,
            JgroupsClusterConfig config,
            RxEventBus eventBus
    ) {
        this.clusterService = clusterService;
        this.config = config;
        this.eventBus = eventBus;
    }

    @Inject
    @Override
    public void activate(ActivatorContext context) {
        log.debug("Activating JGroups cluster.");
        CompletableFuture.supplyAsync(() -> {
            clusterService.joinCluster();
            return clusterService.getSize();
        }).thenAccept(memberCount -> {
            int timeout = 3000;
            if (memberCount == 1) {
                log.info("We are the only member in the cluster. Let's wait {} seconds before we continue " +
                        "in order to make sure that there wasn't a connection problem and we can join " +
                        "an existing cluster.", timeout / 1000);
                try {
                    Thread.sleep(timeout);
                } catch (InterruptedException e) {
                    log.warn(e.getMessage());
                }
            }
        }).thenRun(() -> eventBus.emit(
                ClusterJoinedMessage.builder()
                                    .hostname(clusterService.getName().orElse(""))
                                    .arbiterNode(config.arbiter_enabled())
                                    .build())
        );

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
                .listenFor(TranscodeProgress.class)
                .sample(10, TimeUnit.SECONDS)
                .map(TranscodeProgress::getPercentage)
                .subscribe(clusterService::setProgress));

        handlers.add(eventBus
                .listenFor(MediaInClusterMessage.class)
                .subscribe(this::onMediaInClusterQuery));

        clusterService.getTaskState()
                      .clusterTaskCollectionChanged()
                      .subscribe(eventBus::emit);

        clusterService.onCancelTaskRequested()
                      .subscribe(r -> r.setCancelled(cancelTaskLocally()));

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
