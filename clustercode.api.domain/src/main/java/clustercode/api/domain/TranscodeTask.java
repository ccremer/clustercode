package clustercode.api.domain;

import lombok.*;
import clustercode.api.domain.Media;
import clustercode.api.domain.Profile;

import java.util.List;
import java.util.function.Consumer;

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
