package net.chrigel.clustercode.transcode.impl.ffmpeg;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.chrigel.clustercode.transcode.TranscodeProgress;

import java.time.Duration;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
