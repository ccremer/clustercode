package clustercode.api.transcode.output;

import clustercode.api.transcode.TranscodeReport;
import lombok.*;

import java.time.Duration;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class FfmpegOutput implements TranscodeReport {

    @Getter
    @Setter
    private double percentage;

    @Getter
    private double bitrate;

    @Getter
    private double fps;

    @Getter
    private double fileSize;

    @Getter
    private long frame;

    @Getter
    private double speed;

    @Getter
    private Duration time;

    @Getter
    @Setter
    private Duration duration;

}
