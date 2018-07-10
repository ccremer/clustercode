package clustercode.impl.cluster.jgroups;

import clustercode.api.cluster.messages.CancelTaskApiRequest;
import clustercode.api.event.RxEventBus;
import clustercode.api.transcode.TranscodeProgress;
import clustercode.api.transcode.messages.CancelTranscodeMessage;
import lombok.extern.slf4j.XSlf4j;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

@XSlf4j
public class ClusterActivator {

    private final JgroupsClusterImpl clusterService;
    private final RxEventBus eventBus;

    @Inject
    ClusterActivator(
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
