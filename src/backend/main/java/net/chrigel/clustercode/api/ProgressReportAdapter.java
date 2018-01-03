package net.chrigel.clustercode.api;

import net.chrigel.clustercode.transcode.TranscodeProgress;

import java.util.function.Function;

public interface ProgressReportAdapter
    extends Function<TranscodeProgress, ProgressReport> {

    ProgressReport getReportForInactiveEncoding();

}
