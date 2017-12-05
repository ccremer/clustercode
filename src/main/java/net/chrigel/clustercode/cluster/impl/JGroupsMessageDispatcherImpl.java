package net.chrigel.clustercode.cluster.impl;

import lombok.extern.slf4j.XSlf4j;
import lombok.val;
import net.chrigel.clustercode.cluster.JGroupsMessageDispatcher;
import net.chrigel.clustercode.cluster.messages.CancelTaskMessage;
import net.chrigel.clustercode.cluster.messages.CancelTaskRpcResponse;
import net.chrigel.clustercode.cluster.messages.ClusterMessage;
import net.chrigel.clustercode.cluster.messages.LocalCancelTaskRequest;
import net.chrigel.clustercode.event.Event;
import net.chrigel.clustercode.event.EventBus;
import org.jgroups.JChannel;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.RspList;

import javax.inject.Inject;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
@XSlf4j
public class JGroupsMessageDispatcherImpl
    implements JGroupsMessageDispatcher {

    private final EventBus<ClusterMessage> localBus;
    private RpcDispatcher rpcDispatcher;
    private String localHostname;

    @Inject
    JGroupsMessageDispatcherImpl(EventBus<ClusterMessage> localEventBus) {
        localBus = localEventBus;
    }

    public CancelTaskRpcResponse cancelTaskRpc(String hostname) {
        log.debug("Received hostname: {}", hostname);
        boolean hostnameMatches = hostnameMatchesLocalName(hostname);
        val response = CancelTaskRpcResponse.builder()
            .cancelled(hostnameMatches)
            .build();
        if (hostnameMatches) cancelTaskLocally(hostname);
        return response;
    }

    private boolean hostnameMatchesLocalName(String hostname) {
        return rpcDispatcher.getChannel().getAddressAsString().equals(hostname);
    }

    private void cancelTaskLocally(String hostname) {
        localBus.emit(new Event<>(new CancelTaskMessage(hostname)));
    }

    @Override
    public boolean cancelTask(String hostname) {
        try {
            log.debug("Calling cancelTaskRpc for {}...", hostname);
            MethodCall call = new MethodCall(getClass().getMethod("cancelTaskRpc", String.class));
            RequestOptions ops = new RequestOptions(ResponseMode.GET_ALL, 5000);
            call.setArgs(hostname);
            RspList<CancelTaskRpcResponse> responses = rpcDispatcher.callRemoteMethods(rpcDispatcher.getChannel()
                    .getView()
                    .getMembers()
                    .stream()
                    .filter(h -> h.toString().equalsIgnoreCase(hostname))
                    .collect(Collectors.toList()),
                call, ops);
            log.debug("Got answer: {}", responses);
            return responses.getResults()
                .stream()
                .anyMatch(CancelTaskRpcResponse::isCancelled);
        } catch (Exception e) {
            log.catching(e);
            return false;
        }
    }

    @Override
    public void initialize(JChannel channel, String localHostname) throws Exception {
        this.rpcDispatcher = new RpcDispatcher(channel, this);
        this.localHostname = localHostname;
    }
}
