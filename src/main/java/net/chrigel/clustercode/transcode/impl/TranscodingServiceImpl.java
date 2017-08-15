package net.chrigel.clustercode.transcode.impl;

import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.process.ExternalProcess;
import net.chrigel.clustercode.scan.MediaScanService;
import net.chrigel.clustercode.scan.MediaScanSettings;
import net.chrigel.clustercode.scan.Profile;
import net.chrigel.clustercode.transcode.TranscodeResult;
import net.chrigel.clustercode.transcode.TranscodeTask;
import net.chrigel.clustercode.transcode.TranscoderSettings;
import net.chrigel.clustercode.transcode.TranscodingService;
import net.chrigel.clustercode.util.FileUtil;

import javax.inject.Inject;
import javax.inject.Provider;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@XSlf4j
class TranscodingServiceImpl implements TranscodingService {

    public static final String OUTPUT_PLACEHOLDER = "${OUTPUT}";
    public static final String INPUT_PLACEHOLDER = "${INPUT}";

    private final TranscoderSettings transcoderSettings;
    private MediaScanSettings mediaScanSettings;
    private final Provider<ExternalProcess> externalProcessProvider;

    @Inject
    TranscodingServiceImpl(Provider<ExternalProcess> externalProcessProvider,
                           TranscoderSettings transcoderSettings,
                           MediaScanSettings mediaScanSettings) {
        this.externalProcessProvider = externalProcessProvider;
        this.transcoderSettings = transcoderSettings;
        this.mediaScanSettings = mediaScanSettings;
    }

    Optional<Integer> doTranscode(Path source, Path tempFile, Profile profile) {
        return externalProcessProvider.get()
                .withExecutablePath(transcoderSettings.getTranscoderExecutable())
                .withIORedirected(transcoderSettings.isIoRedirected())
                .withArguments(profile.getArguments().stream()
                        .map(s -> replaceInput(s, source))
                        .map(s -> replaceOutput(s, tempFile))
                        .collect(Collectors.toList()))
                .start();
    }

    @Override
    public TranscodeResult transcode(TranscodeTask task) {
        log.entry(task);

        Path tempFile = transcoderSettings.getTemporaryDir().resolve(
                FileUtil.getFileNameWithoutExtension(task.getMedia().getSourcePath()) +
                        getPropertyOrDefault(task.getProfile(), "FORMAT",
                                transcoderSettings.getDefaultVideoExtension()));

        TranscodeResult result = TranscodeResult.builder()
                .temporaryPath(tempFile)
                .media(task.getMedia())
                .profile(task.getProfile())
                .build();

        log.info("Starting transcoding process...");
        doTranscode(task.getMedia().getSourcePath(), tempFile, task.getProfile())
                .ifPresent(exitCode -> result.setSuccessful(exitCode == 0));

        if (result.isSuccessful()) {
            log.info("Transcoding finished");
        } else {
            log.info("Transcoding failed.");
        }
        return log.exit(result);
    }

    @Override
    public void transcode(TranscodeTask task, Consumer<TranscodeResult> listener) {
        CompletableFuture.runAsync(() -> listener.accept(transcode(task)));
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
