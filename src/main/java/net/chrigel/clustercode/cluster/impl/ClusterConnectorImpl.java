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
        clusterBus.registerEventHandler(CancelTaskMessage.class,
            OptionalFunction.ofNullable(this::onTaskCancelViaRpc));
        clusterBus.registerEventHandler(LocalCancelTaskRequest.class,
            OptionalFunction.ofNullable(this::onTaskCancelRequestedViaApi));
    }

    private boolean isLocalHostnameEqualTo(String otherHostname) {
        String localName = clusterService.getName().orElse(null);
        return otherHostname.equals(localName);
    }

    private Boolean onTaskCancelRequestedViaApi(Event<LocalCancelTaskRequest> event) {
        String hostname = event.getPayload().getHostname();
        if (isLocalHostnameEqualTo(hostname)) return cancelTaskLocally();
        else return cancelTaskInCluster(hostname);
    }

    private Boolean onTaskCancelViaRpc(Event<CancelTaskMessage> event) {
        String hostname = event.getPayload().getHostname();
        return isLocalHostnameEqualTo(hostname) && cancelTaskLocally();
    }

    private boolean cancelTaskLocally() {
        Optional<Boolean> result = transcodeBus.emitAndGet(new Event<>(new CancelTranscodeMessage()));
        return result.orElse(false);
    }

    private boolean cancelTaskInCluster(String hostname) {
        return clusterService.cancelTask(hostname);
    }
}
