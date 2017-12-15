package net.chrigel.clustercode.cluster.impl;

import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.cluster.ClusterConnector;
import net.chrigel.clustercode.cluster.ClusterService;
import net.chrigel.clustercode.cluster.messages.CancelTaskMessage;
import net.chrigel.clustercode.cluster.messages.ClusterMessage;
import net.chrigel.clustercode.cluster.messages.LocalCancelTaskRequest;
import net.chrigel.clustercode.event.Event;
import net.chrigel.clustercode.event.EventBus;
import net.chrigel.clustercode.transcode.messages.CancelTranscodeMessage;
import net.chrigel.clustercode.transcode.messages.ProgressMessage;
import net.chrigel.clustercode.transcode.messages.TranscodeMessage;
import net.chrigel.clustercode.util.OptionalFunction;

import javax.inject.Inject;
import java.util.Optional;

@XSlf4j
public class ClusterConnectorImpl implements ClusterConnector {

    private final ClusterService clusterService;
    private final EventBus<ClusterMessage> clusterBus;
    private final EventBus<TranscodeMessage> transcodeBus;

    @Inject
    ClusterConnectorImpl(
        ClusterService clusterService,
        EventBus<ClusterMessage> clusterBus,
        EventBus<TranscodeMessage> transcodeBus
    ) {
        this.clusterService = clusterService;
        this.clusterBus = clusterBus;
        this.transcodeBus = transcodeBus;
    }

    @Inject
    public void start() {
        clusterBus.registerEventHandler(CancelTaskMessage.class, this::onTaskCancelViaRpc);
        clusterBus.registerEventHandler(LocalCancelTaskRequest.class, this::onTaskCancelRequestedViaApi);

        transcodeBus.registerEventHandler(ProgressMessage.class, this::onProgressUpdate);
    }

    private void onProgressUpdate(Event<ProgressMessage> event) {
        clusterService.setProgress(event.getPayload().getPercentage());
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
        return transcodeBus.emit(new CancelTranscodeMessage()).getAnswer(Boolean.class).orElse(false);
    }

}
