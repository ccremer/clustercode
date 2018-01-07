package net.chrigel.clustercode.cluster.impl;

import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.cluster.ClusterConnector;
import net.chrigel.clustercode.cluster.messages.CancelTaskApiRequest;
import net.chrigel.clustercode.event.RxEventBus;
import net.chrigel.clustercode.transcode.TranscodeProgress;
import net.chrigel.clustercode.transcode.messages.CancelTranscodeMessage;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

@XSlf4j
public class ClusterConnectorImpl implements ClusterConnector {

    private final JgroupsClusterImpl clusterService;
    private final RxEventBus eventBus;

    @Inject
    ClusterConnectorImpl(
        JgroupsClusterImpl clusterService,
        RxEventBus eventBus
    ) {
        this.clusterService = clusterService;
        this.eventBus = eventBus;
    }

    @Inject
    public void start() {

        eventBus.register(CancelTaskApiRequest.class)
                .filter(this::isLocalHost)
                .doOnNext(log::entry)
                .subscribe(r -> r.setCancelled(cancelTaskLocally()));

        eventBus.register(CancelTaskApiRequest.class)
                .filter(this::isNotLocalHost)
                .doOnNext(log::entry)
                .subscribe(r -> r.setCancelled(clusterService.cancelTask(r.getHostname())));

        eventBus.register(TranscodeProgress.class)
                .sample(10, TimeUnit.SECONDS)
                .map(TranscodeProgress::getPercentage)
                .subscribe(clusterService::setProgress);

        clusterService.getTaskState()
                      .clusterTaskCollectionChanged()
                      .subscribe(eventBus::emit);

        clusterService.onCancelTaskRequested()
                      .subscribe(r -> r.setCancelled(cancelTaskLocally()));
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
