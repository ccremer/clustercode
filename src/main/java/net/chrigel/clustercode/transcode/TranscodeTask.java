package net.chrigel.clustercode.transcode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.chrigel.clustercode.scan.Profile;
import net.chrigel.clustercode.scan.Media;

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
