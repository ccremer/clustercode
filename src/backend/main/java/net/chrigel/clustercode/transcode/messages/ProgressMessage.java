package net.chrigel.clustercode.transcode.messages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProgressMessage implements TranscodeMessage {

    private double percentage;

}
