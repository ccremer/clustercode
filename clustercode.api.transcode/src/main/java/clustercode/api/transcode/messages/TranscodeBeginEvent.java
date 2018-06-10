package clustercode.api.transcode.messages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import clustercode.api.transcode.TranscodeTask;

/**
 * This event indicates that a trancoding job has begun.
 */
@Data
@Builder
@AllArgsConstructor
public class TranscodeBeginEvent {

    private TranscodeTask task;

}
