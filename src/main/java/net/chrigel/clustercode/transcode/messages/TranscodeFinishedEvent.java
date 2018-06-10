package net.chrigel.clustercode.transcode.messages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.chrigel.clustercode.scan.Media;
import net.chrigel.clustercode.scan.Profile;

import java.nio.file.Path;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscodeFinishedEvent {

    /**
     * The original media instance.
     */
    private Media media;

    /**
     * The profile used for transcoding.
     */
    private Profile profile;

    /**
     * Whether transcoding was successful (exit code == 0).
     */
    private boolean successful;

    /**
     * The output file written during transcoding.
     */
    private Path temporaryPath;

    /**
     * Whether the task was cancelled.
     */
    private boolean cancelled;

}
