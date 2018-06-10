package clustercode.api.rest.v1.impl;

import clustercode.api.rest.v1.ProgressReportAdapter;
import clustercode.api.rest.v1.dto.FfmpegProgressReport;
import clustercode.api.transcode.TranscodeProgress;
import clustercode.api.transcode.output.FfmpegOutput;
import lombok.val;

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
