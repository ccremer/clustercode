package clustercode.api.transcode.output;

import clustercode.api.transcode.TranscodeProgress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class FfmpegOutput implements TranscodeProgress {

    private double percentage;

    private double bitrate;

    private double fps;

    private double fileSize;

    private long frame;

    private double speed;

    private Duration time;

    private Duration duration;

}
