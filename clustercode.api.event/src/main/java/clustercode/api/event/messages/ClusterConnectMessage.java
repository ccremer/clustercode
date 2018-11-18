package clustercode.api.event.messages;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClusterConnectMessage {

    private String hostname;

    private int clusterSize;

    public boolean isConnected() {
        return clusterSize > 1;
    }
}
