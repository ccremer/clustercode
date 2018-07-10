package clustercode.api.cluster.messages;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CancelTaskApiRequest implements ClusterMessage {

    private String hostname;

    private boolean isCancelled;
}
