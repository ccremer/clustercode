package net.chrigel.clustercode.transcode.impl.ffmpeg;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.chrigel.clustercode.transcode.TranscodeProgress;

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

    private long maxFrame;

    private double speed;

    private String time;

}
