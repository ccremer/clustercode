package net.chrigel.clustercode.transcode;

import java.util.function.Consumer;

public interface TranscodingService {

    /**
     * Performs the transcoding. This method blocks until the process finished or failed.
     *
     * @param task the task, not null.
     * @return the transcoding result
     */
    TranscodeResult transcode(TranscodeTask task);

    /**
     * Runs {@link #transcode(TranscodeTask)} in background.
     *
     * @param task     the task, not null.
     * @param listener the listener instance for retrieving the result.
     */
    void transcode(TranscodeTask task, Consumer<TranscodeResult> listener);

}
