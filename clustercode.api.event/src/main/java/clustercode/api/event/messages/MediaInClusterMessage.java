package clustercode.api.event.messages;

import clustercode.api.domain.Media;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MediaInClusterMessage {

    private Media media;

    private boolean inCluster;

}
