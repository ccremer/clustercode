package net.chrigel.clustercode.api.impl;

import lombok.val;
import net.chrigel.clustercode.api.ProgressReportAdapter;
import net.chrigel.clustercode.api.dto.FfmpegProgressReport;
import net.chrigel.clustercode.transcode.TranscodeProgress;
import net.chrigel.clustercode.transcode.impl.ffmpeg.FfmpegOutput;

public class FfmpegProgressAdapter
    implements ProgressReportAdapter {

    @Override
    public FfmpegProgressReport apply(TranscodeProgress output) {
        val out = (FfmpegOutput) output;
        return FfmpegProgressReport
            .builder()
            .bitrate(out.getBitrate())
            .fps(out.getFps())
            .percentage(out.getPercentage())
            .frame(out.getFrame())
            .duration(out.getDuration().toMillis())
            .time(out.getTime().toMillis())
            .size(out.getFileSize())
            .build();
    }

    @Override
    public FfmpegProgressReport getReportForInactiveEncoding() {
        return FfmpegProgressReport
            .builder()
            .percentage(-1d)
            .fps(-1d)
            .bitrate(-1d)
            .frame(-1L)
            .duration(-1L)
            .time(-1L)
            .size(-1d)
            .build();
    }

}
