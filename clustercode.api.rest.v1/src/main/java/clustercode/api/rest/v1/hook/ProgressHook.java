package clustercode.api.rest.v1.hook;

import clustercode.api.rest.v1.ProgressReport;
import clustercode.api.rest.v1.ProgressReportAdapter;
import clustercode.api.transcode.Transcoder;

/**
 * Represents an interface that hooks into the lifecycle of the state machine. It specifically listens to handbrake
 * or ffmpeg output and caches the latest results.
 */
public interface ProgressHook {

    /**
     * Gets the most recent progress report.
     *
     * @return the output, if an encoding is active. Otherwise returns
     * {@link ProgressReportAdapter#getReportForInactiveEncoding()}.
     */
    ProgressReport getLatestProgressOutput();

    /**
     * Gets the most recent percentage from the transcoding progress.
     *
     * @return a decimal value between 0 and 100, -1 if not job active.
     */
    double getPercentage();

    /**
     * Gets the type of transcoder.
     *
     * @return the type.
     */
    Transcoder getTranscoder();

}
