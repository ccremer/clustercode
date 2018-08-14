package clustercode.api.event.messages;

import clustercode.api.domain.Media;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MediaScannedMessage {

    private List<Media> mediaList;

}
