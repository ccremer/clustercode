package clustercode.impl.transcode;

import clustercode.api.domain.Media;
import clustercode.api.domain.OutputFrameTuple;
import clustercode.api.domain.Profile;
import clustercode.api.domain.TranscodeResult;
import clustercode.api.transcode.TranscodeProgress;
import clustercode.api.transcode.TranscodeReport;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;
import lombok.Getter;

import java.util.function.Consumer;

public class TranscodeProgressImpl implements TranscodeProgress {

    private final Subject<Object> replaySubject;

    TranscodeProgressImpl(Media media, Profile profile) {
        this.media = media;
        this.profile = profile;
        this.replaySubject = ReplaySubject.create().toSerialized();
    }

    @Getter
    private final Media media;

    @Getter
    private final Profile profile;

    @Override
    public TranscodeProgress withStdErrListener(Consumer<String> listener) {
        register(OutputFrameTuple.OutputType.STDERR, listener);
        return this;
    }

    @Override
    public TranscodeProgress withStdOutListener(Consumer<String> listener) {
        register(OutputFrameTuple.OutputType.STDOUT, listener);
        return this;
    }

    private void register(OutputFrameTuple.OutputType type, Consumer<String> listener) {
        replaySubject.ofType(OutputFrameTuple.class)
                     .filter(t -> t.type == type)
                     .map(OutputFrameTuple::getLine)
                     .subscribe(listener::accept);
    }

    @Override
    public TranscodeProgress withOutputListener(Consumer<OutputFrameTuple> listener) {
        replaySubject.ofType(OutputFrameTuple.class)
                     .subscribe(listener::accept);
        return this;
    }

    public void addOutputFrame(OutputFrameTuple outputFrameTuple) {
        replaySubject.onNext(outputFrameTuple);
    }

    public void addTranscodeReport(TranscodeReport report) {
        replaySubject.onNext(report);
    }

    public TranscodeResult setCompleteAndGetResult() {
        replaySubject.onComplete();
        return TranscodeResult.builder()
                .frames(replaySubject.ofType(OutputFrameTuple.class).toList().blockingGet())
                .media(media)
                .profile(profile)
                .build();
    }

}
