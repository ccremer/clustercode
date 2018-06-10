package clustercode.api.rest.v1.impl;

import clustercode.api.rest.v1.ProgressReportAdapter;
import clustercode.api.rest.v1.dto.HandbrakeProgressReport;
import clustercode.api.transcode.TranscodeProgress;
import clustercode.api.transcode.output.HandbrakeOutput;
import lombok.val;

public class HandbrakeProgressAdapter
    implements ProgressReportAdapter {

    @Override
    public HandbrakeProgressReport getReportForInactiveEncoding() {
        return HandbrakeProgressReport
            .builder()
            .averageFps(-1d)
            .eta("00:00:00")
            .fps(-1d)
            .percentage(-1d)
            .build();
    }

    @Override
    public HandbrakeProgressReport apply(TranscodeProgress output) {
        val out = (HandbrakeOutput) output;
        return HandbrakeProgressReport
            .builder()
            .percentage(out.getPercentage())
            .averageFps(out.getAverageFps())
            .fps(out.getFps())
            .eta(out.getEta())
            .build();
    }

}
