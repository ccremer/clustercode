package clustercode.api.event.messages;

import clustercode.api.domain.Media;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MediaSelectedMessage {

    private Media media;

    public boolean isSelected() {
        return media != null;
    }

    public boolean isNotSelected() {
        return media == null;
    }

}
