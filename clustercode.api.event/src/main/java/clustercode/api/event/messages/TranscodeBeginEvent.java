package clustercode.api.event.messages;

import clustercode.api.domain.TranscodeTask;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * This event indicates that a trancoding job has begun.
 */
@Data
@Builder
@AllArgsConstructor
public class TranscodeBeginEvent {

    private TranscodeTask task;

}
