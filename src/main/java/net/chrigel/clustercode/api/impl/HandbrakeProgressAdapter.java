package net.chrigel.clustercode.api.impl;

import lombok.val;
import net.chrigel.clustercode.api.ProgressReportAdapter;
import net.chrigel.clustercode.api.dto.HandbrakeProgressReport;
import net.chrigel.clustercode.transcode.TranscodeProgress;
import net.chrigel.clustercode.transcode.impl.handbrake.HandbrakeOutput;

public class HandbrakeProgressAdapter
implements ProgressReportAdapter<HandbrakeProgressReport> {

    @Override
    public HandbrakeProgressReport getReportForInactiveEncoding() {
        return HandbrakeProgressReport.builder()
                .averageFps(-1d)
                .eta("00:00:00")
                .fps(-1d)
                .percentage(-1d)
                .build();
    }

    @Override
    public HandbrakeProgressReport apply(TranscodeProgress output) {
        val out = (HandbrakeOutput) output;
        return HandbrakeProgressReport.builder()
                .percentage(out.getPercentage())
                .averageFps(out.getAverageFps())
                .fps(out.getFps())
                .eta(out.getEta())
                .build();
    }

}
