package clustercode.api.rest.v1;

import clustercode.api.transcode.TranscodeReport;

import java.util.function.Function;

public interface ProgressReportAdapter
    extends Function<TranscodeReport, ProgressReport> {

    ProgressReport getReportForInactiveEncoding();

}
