package clustercode.api.transcode.output;

import clustercode.api.transcode.TranscodeProgress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class HandbrakeOutput implements TranscodeProgress {

    private double percentage;

    private double fps;

    private double averageFps;

    private String eta;

}
