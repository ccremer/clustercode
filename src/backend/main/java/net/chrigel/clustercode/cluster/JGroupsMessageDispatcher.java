package net.chrigel.clustercode.cluster;

import io.reactivex.Observable;
import net.chrigel.clustercode.cluster.messages.CancelTaskRpcRequest;

public interface JGroupsMessageDispatcher extends JGroupsForkService {

    boolean cancelTask(String hostname);

    Observable<CancelTaskRpcRequest> onRpcTaskCancelled();

}
