package clustercode.api.rest.v1;

import clustercode.api.transcode.TranscodeProgress;

import java.util.function.Function;

public interface ProgressReportAdapter
    extends Function<TranscodeProgress, ProgressReport> {

    ProgressReport getReportForInactiveEncoding();

}
