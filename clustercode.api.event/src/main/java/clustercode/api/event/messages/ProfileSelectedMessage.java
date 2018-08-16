package clustercode.api.event.messages;

import clustercode.api.domain.Media;
import clustercode.api.domain.Profile;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class ProfileSelectedMessage {

    @NonNull
    private Media media;

    private Profile profile;

    public boolean isSelected() {
        return profile != null;
    }

    public boolean isNotSelected() {
        return profile == null;
    }
}
