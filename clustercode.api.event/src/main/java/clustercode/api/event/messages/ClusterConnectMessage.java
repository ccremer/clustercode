package clustercode.api.event.messages;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClusterConnectMessage {

    private String hostname;

    private boolean arbiterNode;

    private int clusterSize;

    public boolean isNotArbiterNode() {
        return !arbiterNode;
    }

    public boolean isConnected() {
        return clusterSize > 1;
    }
}
