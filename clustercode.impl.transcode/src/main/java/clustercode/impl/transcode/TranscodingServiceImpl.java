package clustercode.impl.transcode;

import clustercode.api.domain.Profile;
import clustercode.api.domain.TranscodeTask;
import clustercode.api.event.messages.TranscodeBeginEvent;
import clustercode.api.event.messages.TranscodeFinishedEvent;
import clustercode.api.transcode.TranscodeReport;
import clustercode.api.transcode.TranscodingService;
import clustercode.impl.util.FileUtil;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import lombok.Synchronized;
import lombok.extern.slf4j.XSlf4j;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@XSlf4j
public class TranscodingServiceImpl implements TranscodingService {

    public static final String OUTPUT_PLACEHOLDER = "${OUTPUT}";
    public static final String INPUT_PLACEHOLDER = "${INPUT}";

    private final TranscoderConfig transcoderConfig;
    private final Subject<Object> publisher;

    private boolean cancelRequested;

    @Inject
    TranscodingServiceImpl(TranscoderConfig transcoderConfig) {
        this.transcoderConfig = transcoderConfig;

        this.publisher = PublishSubject.create().toSerialized();

        publisher.ofType(TranscodeTask.class)
                 .observeOn(Schedulers.computation())
                 .subscribeOn(Schedulers.io())
                 .subscribe(this::prepareTranscode);
    }

    @Synchronized
    private void doTranscode(Path tempFile, TranscodeTask task) {

        var source = task.getMedia().getSourcePath();
        log.info("Starting transcoding process: from {} to {}. This might take a while...", source, tempFile);

        publisher.onNext(TranscodeBeginEvent
            .builder()
            .task(task)
            .build());
    }

    private List<String> buildArguments(Path source, Path target, TranscodeTask task) {
        return task.getProfile()
                   .getArguments()
                   .stream()
                   .map(s -> replaceInput(s, source))
                   .map(s -> replaceOutput(s, target))
                   .collect(Collectors.toList());
    }

    private void onSuccess(Path tempFile, TranscodeTask task) {
        log.entry(tempFile, task);
        var event = TranscodeFinishedEvent
            .builder()
            .temporaryPath(tempFile)
            .media(task.getMedia())
            .profile(task.getProfile())
            .successful(true)
            .cancelled(cancelRequested)
            .build();

        cancelRequested = false;

        if (event.isSuccessful()) log.info("Transcode finished.");
        else {
            log.info("Transcode {}.", event.isCancelled() ? "cancelled" : "failed");
        }
        publisher.onNext(event);
    }

    private void onError(Throwable ex) {
        log.error(ex.toString());
        var event = TranscodeFinishedEvent
            .builder()
            .successful(false)
            .build();
        cancelRequested = false;
        publisher.onNext(event);
    }

    private void prepareTranscode(TranscodeTask task) {
        log.entry(task);
        var tempFile = transcoderConfig
            .temporary_dir()
            .resolve(FileUtil.getFileNameWithoutExtension(
                task.getMedia().getSourcePath()) + getPropertyOrDefault(
                task.getProfile(), "FORMAT", transcoderConfig.default_video_extension())
            );

        doTranscode(tempFile, task);
    }

    @Override
    public void transcode(TranscodeTask task) {
        publisher.onNext(task);
    }

    @Override
    @Synchronized
    public boolean cancelTranscode() {
        log.debug("Cancelling task...");
        this.cancelRequested = true;
        return true;
    }

    @Override
    public Flowable<TranscodeBeginEvent> onTranscodeBegin() {
        return publisher
            .subscribeOn(Schedulers.computation())
            .observeOn(Schedulers.computation())
            .ofType(TranscodeBeginEvent.class)
            .toFlowable(BackpressureStrategy.BUFFER);
    }

    @Override
    public Flowable<TranscodeFinishedEvent> onTranscodeFinished() {
        return publisher
            .subscribeOn(Schedulers.computation())
            .observeOn(Schedulers.computation())
            .ofType(TranscodeFinishedEvent.class)
            .toFlowable(BackpressureStrategy.BUFFER);
    }

    @Override
    public Observable<TranscodeReport> onProgressUpdated() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public TranscodingService onProgressUpdated(Consumer<TranscodeReport> listener) {
        publisher
            .ofType(TranscodeReport.class)
            .observeOn(Schedulers.computation())
            .subscribe(listener::accept);
        return this;
    }

    @Override
    public TranscodingService onTranscodeFinished(Consumer<TranscodeFinishedEvent> listener) {
        publisher
            .ofType(TranscodeFinishedEvent.class)
            .observeOn(Schedulers.computation())
            .subscribe(listener::accept);
        return this;
    }

    @Override
    public TranscodingService onTranscodeBegin(Consumer<TranscodeBeginEvent> listener) {
        publisher
            .ofType(TranscodeBeginEvent.class)
            .observeOn(Schedulers.computation())
            .subscribe(listener::accept);
        return this;
    }

    private String getPropertyOrDefault(Profile profile, String key, String defaultValue) {
        return profile.getFields().getOrDefault(key, defaultValue);
    }

    String replaceOutput(String s, Path path) {
        return s.replace(OUTPUT_PLACEHOLDER, path.toString());
    }

    String replaceInput(String s, Path path) {
        return s.replace(INPUT_PLACEHOLDER, transcoderConfig.base_input_dir().resolve(path).toString());
    }

}
