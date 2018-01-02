package net.chrigel.clustercode.transcode;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import net.chrigel.clustercode.transcode.impl.Transcoder;
import net.chrigel.clustercode.transcode.messages.TranscodeBeginEvent;
import net.chrigel.clustercode.transcode.messages.TranscodeFinishedEvent;

import java.util.function.Consumer;

public interface TranscodingService {

    /**
     * Performs the transcoding.
     *
     * @param task the cleanup, not null.
     */
    void transcode(TranscodeTask task);

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

    Flowable<TranscodeBeginEvent> onTranscodeBegin();

    Flowable<TranscodeFinishedEvent> onTranscodeFinished();

    Observable<TranscodeProgress> onProgressUpdated();
}
