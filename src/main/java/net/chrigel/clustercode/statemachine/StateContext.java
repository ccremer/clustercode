package net.chrigel.clustercode.statemachine;

import lombok.Data;
import net.chrigel.clustercode.scan.Media;
import net.chrigel.clustercode.scan.Profile;
import net.chrigel.clustercode.transcode.messages.TranscodeFinishedEvent;

import java.util.List;

/**
 * Represents a state context.
 */
@Data
public class StateContext {

    private List<Media> candidates;

    private Media selectedMedia;

    private Profile selectedProfile;

    private TranscodeFinishedEvent transcodeFinishedEvent;

}
