package clustercode.api.cluster;

import io.reactivex.Observable;
import clustercode.api.cluster.messages.CancelTaskRpcRequest;

public interface JGroupsMessageDispatcher extends JGroupsForkService {

    boolean cancelTask(String hostname);

    Observable<CancelTaskRpcRequest> onRpcTaskCancelled();

}
