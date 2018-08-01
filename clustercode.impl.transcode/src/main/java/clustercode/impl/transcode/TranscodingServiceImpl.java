package clustercode.impl.transcode;

import clustercode.api.domain.Profile;
import clustercode.api.domain.TranscodeTask;
import clustercode.api.event.messages.TranscodeBeginEvent;
import clustercode.api.event.messages.TranscodeFinishedEvent;
import clustercode.api.process.ExternalProcessService;
import clustercode.api.process.ProcessConfiguration;
import clustercode.api.process.RunningExternalProcess;
import clustercode.api.transcode.OutputParser;
import clustercode.api.transcode.TranscodeProgress;
import clustercode.api.transcode.Transcoder;
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
import lombok.val;

import javax.inject.Inject;
import javax.inject.Provider;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@XSlf4j
public class TranscodingServiceImpl implements TranscodingService {

    public static final String OUTPUT_PLACEHOLDER = "${OUTPUT}";
    public static final String INPUT_PLACEHOLDER = "${INPUT}";

    private final TranscoderConfig transcoderConfig;
    private final Provider<OutputParser> parserProvider;
    private final ExternalProcessService externalProcessService;
    private final Subject<Object> publisher;

    private RunningExternalProcess process;
    private boolean cancelRequested;

    @Inject
    TranscodingServiceImpl(ExternalProcessService externalProcessService,
                           TranscoderConfig transcoderConfig,
                           Provider<OutputParser> parserProvider) {
        this.externalProcessService = externalProcessService;
        this.transcoderConfig = transcoderConfig;

        this.parserProvider = parserProvider;

        this.publisher = PublishSubject.create().toSerialized();

        publisher.ofType(TranscodeTask.class)
                 .skipWhile(o -> isActive())
                 .observeOn(Schedulers.computation())
                 .subscribeOn(Schedulers.io())
                 .subscribe(this::prepareTranscode);
    }

    private boolean isActive() {
        return process != null;
    }

    @Synchronized
    private void doTranscode(Path tempFile, TranscodeTask task) {
        if (process != null) return;

        val source = task.getMedia().getSourcePath();
        log.info("Starting transcoding process: from {} to {}. This might take a while...", source, tempFile);

        ProcessConfiguration config = ProcessConfiguration
            .builder()
            .executable(transcoderConfig.transcoder_executable())
            .arguments(task.getProfile()
                           .getArguments()
                           .stream()
                           .map(s -> replaceInput(s, source))
                           .map(s -> replaceOutput(s, tempFile))
                           .collect(Collectors.toList()))
            .stdoutObserver(observable -> observable
                .observeOn(Schedulers.computation())
                .sample(1, TimeUnit.SECONDS)
                .subscribe(parserProvider.get()::parse))
            .build();

        externalProcessService
            .start(config, this::setHandle)
            .subscribe(
                exitCode -> onSuccess(exitCode, tempFile, task),
                this::onError);

        publisher.onNext(TranscodeBeginEvent
            .builder()
            .task(task)
            .build());
    }

    private void onSuccess(Integer exitCode, Path tempFile, TranscodeTask task) {
        log.entry(exitCode, tempFile, task);
        val event = TranscodeFinishedEvent
            .builder()
            .temporaryPath(tempFile)
            .media(task.getMedia())
            .profile(task.getProfile())
            .successful(exitCode == 0)
            .cancelled(cancelRequested)
            .build();

        setHandle(null);
        cancelRequested = false;

        if (event.isSuccessful()) log.info("Transcode finished.");
        else {
            if (event.isCancelled()) log.info("Transcode cancelled.");
            else log.info("Transcode failed.");
        }
        publisher.onNext(event);
    }

    private void onError(Throwable ex) {
        log.error(ex.toString());
        val event = TranscodeFinishedEvent
            .builder()
            .successful(false)
            .build();
        setHandle(null);
        cancelRequested = false;
        publisher.onNext(event);
    }

    @Synchronized
    private void setHandle(RunningExternalProcess process) {
        this.process = process;
    }

    private void prepareTranscode(TranscodeTask task) {
        log.entry(task);
        val tempFile = transcoderConfig
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
        if (process == null) return true;
        log.debug("Cancelling task...");
        this.cancelRequested = true;
        return process.destroyNowWithTimeout(5, TimeUnit.SECONDS);
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
    public Observable<TranscodeProgress> onProgressUpdated() {
        return parserProvider
                .get()
            .onProgressParsed();
    }

    @Override
    public Transcoder getTranscoder() {
        return transcoderConfig.transcoder_type();
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
