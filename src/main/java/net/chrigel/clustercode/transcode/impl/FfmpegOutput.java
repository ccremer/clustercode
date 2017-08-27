package net.chrigel.clustercode.transcode.impl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
class FfmpegOutput {

    private String size;

    private String fps;

    private String bitrate;

    private String frame;

    private String speed;

    private String time;

}
