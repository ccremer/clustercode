package net.chrigel.clustercode.api.impl;

import com.google.inject.Inject;
import net.chrigel.clustercode.api.ApiConnector;
import net.chrigel.clustercode.api.messages.ApiMessage;
import net.chrigel.clustercode.cluster.ClusterService;
import net.chrigel.clustercode.cluster.messages.ClusterMessage;
import net.chrigel.clustercode.cluster.messages.LocalCancelTaskRequest;
import net.chrigel.clustercode.event.Event;
import net.chrigel.clustercode.event.EventBus;
import net.chrigel.clustercode.event.Response;
import net.chrigel.clustercode.transcode.messages.CancelTranscodeMessage;
import net.chrigel.clustercode.transcode.messages.TranscodeMessage;
import net.chrigel.clustercode.util.OptionalFunction;

import java.util.Optional;

public class ApiConnectorImpl implements ApiConnector {

    private final EventBus<TranscodeMessage> transcodeBus;
    private final EventBus<ClusterMessage> clusterBus;
    private final EventBus<ApiMessage> apiBus;
    private final ClusterService clusterService;

    @Inject
    ApiConnectorImpl(EventBus<TranscodeMessage> transcodeBus,
                     EventBus<ClusterMessage> clusterBus,
                     EventBus<ApiMessage> apiBus,
                     ClusterService clusterService) {
        this.transcodeBus = transcodeBus;
        this.clusterBus = clusterBus;
        this.apiBus = apiBus;
        this.clusterService = clusterService;
    }

    @Inject
    public void start() {


        clusterBus.registerEventHandler(LocalCancelTaskRequest.class, this::onTaskCancelRequestedViaApi);

    }

    private boolean isLocalHostnameEqualTo(String otherHostname) {
        String localName = clusterService.getName().orElse("");
        return otherHostname.equals(localName);
    }

    private void onTaskCancelRequestedViaApi(Event<LocalCancelTaskRequest> event) {
        String hostname = event.getPayload().getHostname();
        if (isLocalHostnameEqualTo(hostname)) {
            event.addAnswer(cancelTaskLocally());
        } else {
            event.addAnswer(cancelTaskInCluster(hostname));
        }
    }

    private boolean cancelTaskInCluster(String hostname) {
        return clusterService.cancelTask(hostname);
    }

    private boolean cancelTaskLocally() {
        return transcodeBus.emit(new CancelTranscodeMessage())
            .getAnswer(Boolean.class)
            .orElse(false);
    }

}
