package net.chrigel.clustercode.transcode.impl;

import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.process.ExternalProcess;
import net.chrigel.clustercode.scan.Profile;
import net.chrigel.clustercode.task.Media;
import net.chrigel.clustercode.transcode.TranscoderSettings;
import net.chrigel.clustercode.transcode.TranscodingService;

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
    private final Provider<ExternalProcess> externalProcessProvider;

    @Inject
    TranscodingServiceImpl(Provider<ExternalProcess> externalProcessProvider,
                           TranscoderSettings transcoderSettings) {
        this.externalProcessProvider = externalProcessProvider;
        this.transcoderSettings = transcoderSettings;
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
    public boolean transcode(Media candidate, Profile profile) {
        log.entry(candidate, profile);

        Path tempFile = transcoderSettings.getTemporaryDir().resolve(
                getFileNameWithoutExtension(candidate.getSourcePath()) +
                        getPropertyOrDefault(profile, "FORMAT", transcoderSettings.getDefaultVideoExtension()));

        profile.setTemporaryFile(tempFile);
        log.info("Starting transcoding process...");
        Optional<Integer> exitCode = doTranscode(candidate.getSourcePath(), tempFile, profile);

        if (exitCode.isPresent() && exitCode.get() == 0) {
            log.info("Transcoding finished");
            return log.exit(true);
        } else {
            log.info("Transcoding failed.");
            return log.exit(false);
        }
    }

    @Override
    public void transcode(Media candidate, Profile profile, Consumer<Boolean> listener) {
        CompletableFuture.runAsync(() -> listener.accept(transcode(candidate, profile)));
    }

    private String getPropertyOrDefault(Profile profile, String key, String defaultValue) {
        return profile.getFields().getOrDefault(key, defaultValue);
    }

    String replaceOutput(String s, Path path) {
        return s.replace(OUTPUT_PLACEHOLDER, path.toString());
    }

    String replaceInput(String s, Path path) {
        return s.replace(INPUT_PLACEHOLDER, path.toString());
    }

    String getFileNameWithoutExtension(Path path) {
        String name = path.getFileName().toString();
        int index = name.lastIndexOf('.');
        if (index <= 0) {
            return name;
        } else {
            return name.substring(0, index);
        }
    }
}
