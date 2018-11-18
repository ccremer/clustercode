package clustercode.api.transcode;

import clustercode.api.domain.TranscodeTask;
import clustercode.api.event.messages.TranscodeBeginEvent;
import clustercode.api.event.messages.TranscodeFinishedEvent;
import io.reactivex.Flowable;
import io.reactivex.Observable;

import java.util.function.Consumer;

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

    Observable<TranscodeReport> onProgressUpdated();

    TranscodingService onTranscodeBegin(Consumer<TranscodeBeginEvent> listener);

    TranscodingService onTranscodeFinished(Consumer<TranscodeFinishedEvent> listener);

    TranscodingService onProgressUpdated(Consumer<TranscodeReport> listener);

}
