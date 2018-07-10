package clustercode.api.cluster.messages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CancelTaskRpcRequest implements Serializable {

    private boolean cancelled;

    private String hostname;
}
