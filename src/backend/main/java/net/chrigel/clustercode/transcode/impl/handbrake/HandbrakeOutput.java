package net.chrigel.clustercode.transcode.impl.handbrake;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.chrigel.clustercode.transcode.TranscodeProgress;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HandbrakeOutput implements TranscodeProgress {

    private double percentage;

    private double fps;

    private double averageFps;

    private String eta;

}
