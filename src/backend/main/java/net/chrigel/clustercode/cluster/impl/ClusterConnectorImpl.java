package net.chrigel.clustercode.cluster.impl;

import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.cluster.ClusterConnector;
import net.chrigel.clustercode.cluster.messages.CancelTaskMessage;
import net.chrigel.clustercode.cluster.messages.ClusterMessage;
import net.chrigel.clustercode.cluster.messages.LocalCancelTaskRequest;
import net.chrigel.clustercode.event.Event;
import net.chrigel.clustercode.event.EventBus;
import net.chrigel.clustercode.event.RxEventBus;
import net.chrigel.clustercode.transcode.impl.ffmpeg.FfmpegOutput;
import net.chrigel.clustercode.transcode.impl.handbrake.HandbrakeOutput;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

@XSlf4j
public class ClusterConnectorImpl implements ClusterConnector {

    private final JgroupsClusterImpl clusterService;
    private final EventBus<ClusterMessage> clusterBus;
    private final RxEventBus eventBus;

    @Inject
    ClusterConnectorImpl(
        JgroupsClusterImpl clusterService,
        EventBus<ClusterMessage> clusterBus,
        RxEventBus eventBus
    ) {
        this.clusterService = clusterService;
        this.clusterBus = clusterBus;
        this.eventBus = eventBus;
    }

    @Inject
    public void start() {
        clusterBus.registerEventHandler(CancelTaskMessage.class, this::onTaskCancelViaRpc);
        clusterBus.registerEventHandler(LocalCancelTaskRequest.class, this::onTaskCancelRequestedViaApi);

        eventBus.register(HandbrakeOutput.class)
                .sample(10, TimeUnit.SECONDS)
                .map(HandbrakeOutput::getPercentage)
                .subscribe(clusterService::setProgress);

        eventBus.register(FfmpegOutput.class)
                .sample(10, TimeUnit.SECONDS)
                .map(FfmpegOutput::getPercentage)
                .subscribe(clusterService::setProgress);

        clusterService.getTaskState()
                      .clusterTaskCollectionChanged()
                      .subscribe(eventBus::emit);
    }

    private boolean isLocalHostnameEqualTo(String otherHostname) {
        String localName = clusterService.getName().orElse(null);
        return otherHostname.equals(localName);
    }

    private void onTaskCancelViaRpc(Event<CancelTaskMessage> event) {
        String hostname = event.getPayload().getHostname();
        event.addAnswer(isLocalHostnameEqualTo(hostname) && cancelTaskLocally());
    }

    private void onTaskCancelRequestedViaApi(Event<LocalCancelTaskRequest> event) {
        String hostname = event.getPayload().getHostname();
        if (isLocalHostnameEqualTo(hostname)) {
            event.addAnswer(cancelTaskLocally());
        } else {
            event.addAnswer(clusterService.cancelTask(hostname));
        }
    }

    private boolean cancelTaskLocally() {
        // return transcodeBus.emit(new CancelTranscodeMessage()).getAnswer(Boolean.class).orElse(false);
        return false;
    }

}
