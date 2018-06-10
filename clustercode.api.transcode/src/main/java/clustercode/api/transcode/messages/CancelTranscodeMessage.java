package clustercode.api.transcode.messages;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class CancelTranscodeMessage {

    private boolean cancelled;

}
