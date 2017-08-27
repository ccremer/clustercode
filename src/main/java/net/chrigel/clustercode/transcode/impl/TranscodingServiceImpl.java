package net.chrigel.clustercode.transcode.impl;

import lombok.Synchronized;
import lombok.extern.slf4j.XSlf4j;
import lombok.val;
import net.chrigel.clustercode.process.ExternalProcess;
import net.chrigel.clustercode.process.OutputParser;
import net.chrigel.clustercode.scan.MediaScanSettings;
import net.chrigel.clustercode.scan.Profile;
import net.chrigel.clustercode.transcode.*;
import net.chrigel.clustercode.util.FileUtil;
import net.chrigel.clustercode.util.Platform;

import javax.inject.Inject;
import javax.inject.Provider;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@XSlf4j
class TranscodingServiceImpl implements TranscodingService {

    public static final String OUTPUT_PLACEHOLDER = "${OUTPUT}";
    public static final String INPUT_PLACEHOLDER = "${INPUT}";

    private final TranscoderSettings transcoderSettings;
    private final MediaScanSettings mediaScanSettings;
    private final OutputParser<FfmpegOutput> ffmpegParser;
    private final OutputParser<FfprobeOutput> ffprobeParser;
    private final ProgressCalculator progressCalculator;
    private final Provider<ExternalProcess> externalProcessProvider;
    private boolean active;

    @Inject
    TranscodingServiceImpl(Provider<ExternalProcess> externalProcessProvider,
                           TranscoderSettings transcoderSettings,
                           MediaScanSettings mediaScanSettings,
                           OutputParser<FfmpegOutput> ffmpegParser,
                           OutputParser<FfprobeOutput> ffprobeParser,
                           ProgressCalculator progressCalculator) {
        this.externalProcessProvider = externalProcessProvider;
        this.transcoderSettings = transcoderSettings;
        this.mediaScanSettings = mediaScanSettings;
        this.ffmpegParser = ffmpegParser;
        this.ffprobeParser = ffprobeParser;
        this.progressCalculator = progressCalculator;

        this.ffmpegParser.register(progressCalculator);
    }

    @Synchronized
    Optional<Integer> doTranscode(Path source, Path tempFile, Profile profile) {
        this.active = true;
        return externalProcessProvider.get()
                .withExecutablePath(transcoderSettings.getTranscoderExecutable())
                .withIORedirected(transcoderSettings.isIoRedirected())
                .withArguments(profile.getArguments().stream()
                        .map(s -> replaceInput(s, source))
                        .map(s -> replaceOutput(s, tempFile))
                        .collect(Collectors.toList()))
                .withStderrParser(createParser())
                .withStdoutParser(createParser())
                .start();
    }

    void determineFrameCount(Path source) {
        if (Platform.currentPlatform() == Platform.WINDOWS) return;
        log.debug("Trying to get the total frame count...");
        ffprobeParser.start();
        val exitCode = externalProcessProvider.get()
                .withExecutablePath(Paths.get("/usr", "bin", "ffprobe"))
                .withIORedirected(false)
                .withArguments(Arrays.asList(
                        "-v", "error",
                        "-count_frames",
                        "-select_streams", "v:0",
                        "-show_entries", "stream=nb_read_frames",
                        "-of", "default=nokey=1:noprint_wrappers=1",
                        mediaScanSettings.getBaseInputDir().resolve(source).toString()))
                .withStdoutParser(ffprobeParser)
                .start();
        long frameCount = -1L;
        if (probingWasSuccessful(exitCode)) frameCount = ffprobeParser.getResult().get().getFrameCount();
        log.debug("Frames counted: {}", frameCount);
        progressCalculator.setFrameCount(frameCount);
        ffprobeParser.stop();
    }

    private boolean probingWasSuccessful(Optional<Integer> exitCode) {
        return exitCode.isPresent() && exitCode.get() == 0 && ffprobeParser.getResult().isPresent();
    }

    @Override
    public TranscodeResult transcode(TranscodeTask task) {
        log.entry(task);

        val tempFile = transcoderSettings.getTemporaryDir().resolve(
                FileUtil.getFileNameWithoutExtension(task.getMedia().getSourcePath()) +
                        getPropertyOrDefault(task.getProfile(), "FORMAT",
                                transcoderSettings.getDefaultVideoExtension()));

        val result = TranscodeResult.builder()
                .temporaryPath(tempFile)
                .media(task.getMedia())
                .successful(false)
                .profile(task.getProfile())
                .build();

        val source = task.getMedia().getSourcePath();
        determineFrameCount(source);
        log.info("Starting transcoding process: from {} to {}. This might take a while...", source, tempFile);
        doTranscode(source, tempFile, task.getProfile())
                .ifPresent(exitCode -> result.setSuccessful(exitCode == 0));
        this.active = false;
        log.info(result.isSuccessful() ? "Transcoding finished" : "Transcoding failed.");
        return log.exit(result);
    }

    @Override
    public void transcode(TranscodeTask task, Consumer<TranscodeResult> listener) {
        CompletableFuture.runAsync(() -> listener.accept(transcode(task)));
    }

    @Override
    public Optional<TranscodeProgress> getCurrentProgress() {
        if (!active) return Optional.empty();
        return progressCalculator.getProgress();
    }

    @Override
    public boolean isActive() {
        return active;
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

    private RedirectedParser<FfmpegOutput> createParser() {
        if (transcoderSettings.isIoRedirected()) return null;
        return new RedirectedParser<>(ffmpegParser);
    }
}
