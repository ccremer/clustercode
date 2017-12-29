package net.chrigel.clustercode.transcode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.chrigel.clustercode.scan.Media;
import net.chrigel.clustercode.scan.Profile;

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

    /**
     * Whether the task has been accepted for transcoding.
     */
    private boolean accepted;
}
