package clustercode.api.event.messages;

import clustercode.api.domain.Media;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
@Builder
public class MediaScannedMessage {

    @NonNull
    private List<Media> mediaList;

    public boolean listIsEmpty() {
        return mediaList.isEmpty();
    }

    public boolean listHasEntries() {
        return !mediaList.isEmpty();
    }
}
