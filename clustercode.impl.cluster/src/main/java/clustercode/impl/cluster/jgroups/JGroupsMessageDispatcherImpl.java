package clustercode.impl.cluster;

import clustercode.api.cluster.JGroupsMessageDispatcher;
import clustercode.api.cluster.messages.CancelTaskRpcRequest;
import clustercode.api.cluster.messages.CancelTaskRpcResponse;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import lombok.extern.slf4j.XSlf4j;
import org.jgroups.JChannel;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.RspList;

import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
@XSlf4j
public class JGroupsMessageDispatcherImpl
    implements JGroupsMessageDispatcher {

    private final Subject<Object> publisher =PublishSubject.create().toSerialized();
    private RpcDispatcher rpcDispatcher;

    public CancelTaskRpcResponse cancelTaskRpc(String hostname) {
        log.entry(hostname);
        CancelTaskRpcRequest request = CancelTaskRpcRequest
            .builder()
            .hostname(hostname)
            .build();
        publisher.onNext(request);
        return log.exit(CancelTaskRpcResponse
            .builder()
            .cancelled(request.isCancelled())
            .build());
    }

    @Override
    public boolean cancelTask(String hostname) {
        try {
            log.debug("Calling cancelTaskRpc for {}...", hostname);
            MethodCall call = new MethodCall(getClass().getMethod("cancelTaskRpc", String.class));
            RequestOptions ops = new RequestOptions(ResponseMode.GET_ALL, 5000);
            call.setArgs(hostname);
            RspList<CancelTaskRpcResponse> responses = rpcDispatcher.callRemoteMethods(
                rpcDispatcher.getChannel()
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
    public Observable<CancelTaskRpcRequest> onRpcTaskCancelled() {
        return publisher.ofType(CancelTaskRpcRequest.class);
    }

    @Override
    public void initialize(JChannel channel, String localHostname) throws Exception {
        this.rpcDispatcher = new RpcDispatcher(channel, this);
    }
}
