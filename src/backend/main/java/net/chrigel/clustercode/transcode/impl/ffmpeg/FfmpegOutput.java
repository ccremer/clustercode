package net.chrigel.clustercode.transcode.impl.ffmpeg;

import lombok.*;
import net.chrigel.clustercode.transcode.TranscodeProgress;

import java.time.Duration;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter(value = AccessLevel.PACKAGE)
@EqualsAndHashCode
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
