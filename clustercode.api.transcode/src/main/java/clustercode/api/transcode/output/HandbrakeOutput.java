package clustercode.api.transcode.output;

import clustercode.api.transcode.TranscodeReport;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class HandbrakeOutput implements TranscodeReport {

    @Getter
    private double percentage;

    @Getter
    private double fps;

    @Getter
    private double averageFps;

    @Getter
    private String eta;

}
