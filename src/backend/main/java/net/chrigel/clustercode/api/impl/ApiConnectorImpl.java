package net.chrigel.clustercode.api.impl;

import com.google.inject.Inject;
import net.chrigel.clustercode.api.ApiConnector;
import net.chrigel.clustercode.cluster.ClusterService;
import net.chrigel.clustercode.cluster.messages.ClusterMessage;
import net.chrigel.clustercode.cluster.messages.LocalCancelTaskRequest;
import net.chrigel.clustercode.event.Event;
import net.chrigel.clustercode.event.EventBus;

public class ApiConnectorImpl implements ApiConnector {

    private final EventBus<ClusterMessage> clusterBus;
    private final ClusterService clusterService;

    @Inject
    ApiConnectorImpl(EventBus<ClusterMessage> clusterBus,
                     ClusterService clusterService) {
        this.clusterBus = clusterBus;
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
         //   event.addAnswer(cancelTaskLocally());
        } else {
            event.addAnswer(cancelTaskInCluster(hostname));
        }
    }

    private boolean cancelTaskInCluster(String hostname) {
        return clusterService.cancelTask(hostname);
    }

}
