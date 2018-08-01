package clustercode.impl.cluster.jgroups;

import clustercode.api.cluster.messages.CancelTaskApiRequest;
import clustercode.api.domain.Activator;
import clustercode.api.domain.ActivatorContext;
import clustercode.api.event.RxEventBus;
import clustercode.api.event.messages.CancelTranscodeMessage;
import clustercode.api.transcode.TranscodeProgress;
import lombok.extern.slf4j.XSlf4j;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

@XSlf4j
public class ClusterActivator implements Activator {

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

    @Override
    public void activate(ActivatorContext context) {
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

    @Override
    public void deactivate(ActivatorContext context) {

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
