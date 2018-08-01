package clustercode.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import clustercode.api.domain.Media;
import clustercode.api.domain.Profile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscodeTask {

    /**
     * The source media object.
     */
    private Media media;

    /**
     * The profile to use for transcoding.
     */
    private Profile profile;

}
