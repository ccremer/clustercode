package clustercode.api.transcode;

import clustercode.api.transcode.messages.TranscodeBeginEvent;
import clustercode.api.transcode.messages.TranscodeFinishedEvent;
import io.reactivex.Flowable;
import io.reactivex.Observable;

public interface TranscodingService {

    /**
     * Performs the transcoding.
     *
     * @param task the cleanup, not null.
     */
    void transcode(TranscodeTask task);

    /**
     * Cancels the current transcoding job. Does nothing if no transcoding active.
     *
     * @return true if the job has been cancelled or none active. False if failed or cancellation timed out.
     */
    boolean cancelTranscode();

    Flowable<TranscodeBeginEvent> onTranscodeBegin();

    Flowable<TranscodeFinishedEvent> onTranscodeFinished();

    Observable<TranscodeProgress> onProgressUpdated();
}
