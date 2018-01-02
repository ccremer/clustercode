package net.chrigel.clustercode.transcode.impl;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import lombok.Synchronized;
import lombok.extern.slf4j.XSlf4j;
import lombok.val;
import net.chrigel.clustercode.process.ExternalProcessService;
import net.chrigel.clustercode.transcode.OutputParser;
import net.chrigel.clustercode.process.ProcessConfiguration;
import net.chrigel.clustercode.process.RunningExternalProcess;
import net.chrigel.clustercode.scan.MediaScanSettings;
import net.chrigel.clustercode.scan.Profile;
import net.chrigel.clustercode.transcode.TranscodeTask;
import net.chrigel.clustercode.transcode.TranscoderSettings;
import net.chrigel.clustercode.transcode.TranscodingService;
import net.chrigel.clustercode.transcode.messages.TranscodeBeginEvent;
import net.chrigel.clustercode.transcode.messages.TranscodeFinishedEvent;
import net.chrigel.clustercode.util.FileUtil;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@XSlf4j
class TranscodingServiceImpl implements TranscodingService {

    public static final String OUTPUT_PLACEHOLDER = "${OUTPUT}";
    public static final String INPUT_PLACEHOLDER = "${INPUT}";

    private final TranscoderSettings transcoderSettings;
    private final MediaScanSettings mediaScanSettings;
    private final OutputParser parser;
    private final ExternalProcessService externalProcessService;
    private final Subject<Object> publisher;
    private RunningExternalProcess process;
    private boolean cancelRequested;

    @Inject
    TranscodingServiceImpl(ExternalProcessService externalProcessService,
                           TranscoderSettings transcoderSettings,
                           MediaScanSettings mediaScanSettings,
                           OutputParser parser) {
        this.externalProcessService = externalProcessService;
        this.transcoderSettings = transcoderSettings;
        this.mediaScanSettings = mediaScanSettings;

        this.parser = parser;

        this.publisher = PublishSubject.create().toSerialized();

        publisher.filter(TranscodeTask.class::isInstance)
                 .skipWhile(o -> isActive())
                 .cast(TranscodeTask.class)
                 .observeOn(Schedulers.computation())
                 .subscribeOn(Schedulers.io())
                 .subscribe(this::prepareTranscode);
    }

    @Synchronized
    void doTranscode(Path tempFile, TranscodeTask task) {
        if (process != null) return;

        val source = task.getMedia().getSourcePath();
        log.info("Starting transcoding process: from {} to {}. This might take a while...", source, tempFile);

        ProcessConfiguration config = ProcessConfiguration
            .builder()
            .executable(transcoderSettings.getTranscoderExecutable())
            .arguments(task.getProfile()
                           .getArguments()
                           .stream()
                           .map(s -> replaceInput(s, source))
                           .map(s -> replaceOutput(s, tempFile))
                           .collect(Collectors.toList()))
            .stdoutObserver(observable -> observable
                .sample(1, TimeUnit.SECONDS)
                .subscribe(parser::parse))
            .build();

        externalProcessService
            .start(config, this::setHandle)
            .subscribe(
                exitCode -> onSuccess(exitCode, tempFile, task),
                this::onError);

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

    private void onSuccess(Integer exitCode, Path tempFile, TranscodeTask task) {
        val event = TranscodeFinishedEvent
            .builder()
            .temporaryPath(tempFile)
            .media(task.getMedia())
            .profile(task.getProfile())
            .build();

        event.setSuccessful(exitCode == 0);
        setHandle(null);
        event.setCancelled(cancelRequested);
        cancelRequested = false;

        log.info(event.isSuccessful() ? "Transcoding finished" : "Transcoding failed or cancelled.");
        publisher.onNext(event);
    }

    @Synchronized
    private void setHandle(RunningExternalProcess process) {
        this.process = process;
    }

    private void prepareTranscode(TranscodeTask task) {
        log.entry(task);
        val tempFile = transcoderSettings
            .getTemporaryDir()
            .resolve(FileUtil.getFileNameWithoutExtension(
                task.getMedia().getSourcePath()) + getPropertyOrDefault(
                task.getProfile(), "FORMAT", transcoderSettings.getDefaultVideoExtension())
            );

        doTranscode(tempFile, task);
    }

    @Override
    public void transcode(TranscodeTask task) {
        publisher.onNext(task);
    }

    @Override
    public Transcoder getTranscoder() {
        return transcoderSettings.getTranscoderType();
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
    public boolean isActive() {
        return process != null;
    }

    @Override
    public Flowable<TranscodeBeginEvent> transcodeBegin() {
        return publisher.ofType(TranscodeBeginEvent.class)
                        .toFlowable(BackpressureStrategy.BUFFER)
                        .observeOn(Schedulers.computation());
    }

    @Override
    public Flowable<TranscodeFinishedEvent> transcodeFinished() {
        return publisher.ofType(TranscodeFinishedEvent.class)
                        .toFlowable(BackpressureStrategy.BUFFER)
                        .observeOn(Schedulers.computation());
    }

    private String getPropertyOrDefault(Profile profile, String key, String defaultValue) {
        return profile.getFields().getOrDefault(key, defaultValue);
    }

    String replaceOutput(String s, Path path) {
        return s.replace(OUTPUT_PLACEHOLDER, path.toString());
    }

    String replaceInput(String s, Path path) {
        return s.replace(INPUT_PLACEHOLDER, mediaScanSettings.getBaseInputDir().resolve(path).toString());
    }

}
