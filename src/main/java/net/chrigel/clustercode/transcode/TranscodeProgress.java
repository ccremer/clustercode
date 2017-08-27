package net.chrigel.clustercode.transcode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TranscodeProgress {

    private double percentage;

    private double bitrate;

    private double fps;

    private double fileSize;

    private long frame;

    private long maxFrame;

    private double speed;

    private String time;

}
