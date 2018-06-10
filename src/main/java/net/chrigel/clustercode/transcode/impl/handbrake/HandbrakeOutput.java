package net.chrigel.clustercode.transcode.impl.handbrake;

import lombok.*;
import net.chrigel.clustercode.transcode.TranscodeProgress;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter(value = AccessLevel.PACKAGE)
@EqualsAndHashCode
@ToString
public class HandbrakeOutput implements TranscodeProgress {

    private double percentage;

    private double fps;

    private double averageFps;

    private String eta;

}
