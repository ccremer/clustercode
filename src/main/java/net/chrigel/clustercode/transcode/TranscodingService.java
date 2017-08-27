package net.chrigel.clustercode.transcode;

import java.util.Optional;
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
     * Gets the current progress of the task.
     *
     * @return the progress, otherwise empty.
     */
    Optional<TranscodeProgress> getCurrentProgress();

    /**
     * Returns true if task is in active transcoding.
     */
    boolean isActive();

}
