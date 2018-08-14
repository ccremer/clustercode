package clustercode.api.event.messages;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClusterJoinedMessage {

    private String hostname;

}
