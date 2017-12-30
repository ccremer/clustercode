package net.chrigel.clustercode.transcode.impl;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import lombok.Synchronized;
import lombok.extern.slf4j.XSlf4j;
import lombok.val;
import net.chrigel.clustercode.process.ExternalProcess;
import net.chrigel.clustercode.process.OutputParser;
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
import javax.inject.Provider;
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
    private final Provider<ExternalProcess> externalProcessProvider;
    private final Subject<Object> publisher;
    private RunningExternalProcess process;
    private boolean cancelRequested;

    @Inject
    TranscodingServiceImpl(Provider<ExternalProcess> externalProcessProvider,
                           TranscoderSettings transcoderSettings,
                           MediaScanSettings mediaScanSettings,
                           OutputParser parser) {
        this.externalProcessProvider = externalProcessProvider;
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
    Optional<Integer> doTranscode(Path source, Path tempFile, Profile profile) {
        log.info("Starting transcoding process: from {} to {}. This might take a while...", source, tempFile);
        this.process = externalProcessProvider.get()
                                              .withExecutablePath(transcoderSettings.getTranscoderExecutable())
                                              .withIORedirected(transcoderSettings.isIoRedirected())
                                              .withArguments(profile.getArguments().stream()
                                                                    .map(s -> replaceInput(s, source))
                                                                    .map(s -> replaceOutput(s, tempFile))
                                                                    .collect(Collectors.toList()))
                                              .withStderrParser(parser)
                                              .withStdoutParser(parser)
                                              .startInBackground();
        return process.waitFor();
    }

    private void prepareTranscode(TranscodeTask task) {
        log.entry(task);

        val tempFile = transcoderSettings.getTemporaryDir().resolve(
            FileUtil.getFileNameWithoutExtension(task.getMedia().getSourcePath()) +
                getPropertyOrDefault(task.getProfile(), "FORMAT",
                    transcoderSettings.getDefaultVideoExtension()));

        val result = TranscodeFinishedEvent.builder()
                                           .temporaryPath(tempFile)
                                           .media(task.getMedia())
                                           .profile(task.getProfile())
                                           .build();

        val source = task.getMedia().getSourcePath();
        doTranscode(source, tempFile, task.getProfile())
            .ifPresent(exitCode -> result.setSuccessful(exitCode == 0));
        if (cancelRequested) {
            result.setCancelled(true);
            cancelRequested = false;
        }
        process = null;
        log.info(result.isSuccessful() ? "Transcoding finished" : "Transcoding failed or cancelled.");
        publisher.onNext(result);
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
        return publisher.filter(TranscodeBeginEvent.class::isInstance)
                        .cast(TranscodeBeginEvent.class)
                        .toFlowable(BackpressureStrategy.BUFFER)
                        .observeOn(Schedulers.computation());
    }

    @Override
    public Flowable<TranscodeFinishedEvent> transcodeFinished() {
        return publisher.filter(TranscodeFinishedEvent.class::isInstance)
                        .cast(TranscodeFinishedEvent.class)
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
