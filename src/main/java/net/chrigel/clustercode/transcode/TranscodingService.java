package net.chrigel.clustercode.transcode;

import net.chrigel.clustercode.transcode.impl.ProgressCalculator;
import net.chrigel.clustercode.transcode.impl.Transcoder;

import java.util.function.Consumer;

public interface TranscodingService {

    /**
     * Performs the transcoding. This method blocks until the process finished or failed.
     *
     * @param task the cleanup, not null.
     * @return the transcoding result
     */
    TranscodeResult transcode(TranscodeTask task);

    /**
     * Runs {@link #transcode(TranscodeTask)} in background.
     *
     * @param task     the cleanup, not null.
     * @param listener the listener instance for retrieving the result.
     */
    void transcode(TranscodeTask task, Consumer<TranscodeResult> listener);

    /**
     * Gets the current progress calculator of the task.
     *
     * @return the calculator, otherwise empty.
     */
    ProgressCalculator getProgressCalculator();

    /**
     * Gets the type of the locally configured transcoder.
     *
     * @return the enum type.
     */
    Transcoder getTranscoder();

    /**
     * Cancels the current transcoding job. Does nothing if no transcoding active.
     *
     * @return true if the job has been cancelled or none active. False if failed or cancellation timed out.
     */
    boolean cancelTranscode();

    /**
     * Whether a conversion is currently active.
     *
     * @return true if active, otherwise false.
     */
    boolean isActive();

}
