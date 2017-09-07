package net.chrigel.clustercode.cluster.impl;

import lombok.extern.slf4j.XSlf4j;
import lombok.val;
import net.chrigel.clustercode.cluster.JGroupsMessageDispatcher;
import net.chrigel.clustercode.cluster.messages.CancelTaskResponse;
import net.chrigel.clustercode.cluster.messages.ClusterMessage;
import net.chrigel.clustercode.cluster.messages.LocalCancelTaskRequest;
import net.chrigel.clustercode.event.Event;
import net.chrigel.clustercode.event.EventBus;
import org.jgroups.JChannel;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;

import javax.inject.Inject;

@XSlf4j
public class JGroupsMessageDispatcherImpl
    implements JGroupsMessageDispatcher {

    private final EventBus<ClusterMessage> localBus;
    private RpcDispatcher rpcDispatcher;

    @Inject
    JGroupsMessageDispatcherImpl(EventBus<ClusterMessage> localBus) {
        this.localBus = localBus;
    }

    public CancelTaskResponse cancelTaskRpc(String hostname) {
        log.debug("Received hostname: {}", hostname);
        boolean hostnameMatches = hostnameMatchesLocalName(hostname);
        val response = CancelTaskResponse.builder()
            .cancelled(hostnameMatches)
            .build();
        if (hostnameMatches) cancelTaskLocally();
        return response;
    }

    private boolean hostnameMatchesLocalName(String hostname) {
        return rpcDispatcher.getChannel().getAddressAsString().equals(hostname);
    }

    private void cancelTaskLocally() {
        localBus.emit(new Event<>(this, new LocalCancelTaskRequest()));
    }

    @Override
    public boolean cancelTask(String hostname) {
        if (rpcDispatcher == null || hostname.equals(rpcDispatcher.getChannel().getAddressAsString())) {
            cancelTaskLocally();
            return true;
        }
        try {
            log.debug("Calling RPC...");
            MethodCall call = new MethodCall(getClass().getMethod("cancelTaskRpc", String.class));
            RequestOptions ops = new RequestOptions(ResponseMode.GET_ALL, 5000);
            call.setArgs(hostname);
            val responses = rpcDispatcher.callRemoteMethods(null, call, ops);
            log.debug("Got answer: {}", responses);
            return responses.getResults()
                .stream()
                .map(o -> (CancelTaskResponse) o)
                .anyMatch(CancelTaskResponse::isCancelled);
        } catch (Exception e) {
            log.catching(e);
            return false;
        }
    }

    @Override
    public void initialize(JChannel channel) {
        this.rpcDispatcher = new RpcDispatcher(channel, this);
    }
}
