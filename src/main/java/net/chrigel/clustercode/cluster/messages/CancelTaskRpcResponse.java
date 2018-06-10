package net.chrigel.clustercode.cluster.messages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CancelTaskRpcResponse implements Serializable {

    private boolean cancelled;

}
