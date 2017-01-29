package net.chrigel.clustercode.transcode.impl;

import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.process.ExternalProcess;
import net.chrigel.clustercode.scan.Profile;
import net.chrigel.clustercode.task.Media;
import net.chrigel.clustercode.transcode.TranscoderSettings;
import net.chrigel.clustercode.transcode.TranscodingService;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@XSlf4j
class TranscodingServiceImpl implements TranscodingService {

    private final Provider<ExternalProcess> externalProcessProvider;
    private final TranscoderSettings transcoderSettings;

    @Inject
    TranscodingServiceImpl(Provider<ExternalProcess> externalProcessProvider,
                           TranscoderSettings transcoderSettings) {
        this.externalProcessProvider = externalProcessProvider;
        this.transcoderSettings = transcoderSettings;
    }

    private ExternalProcess prepareProcess(List<String> arguments) {
        return externalProcessProvider.get()
                .withArguments(arguments)
                .withIORedirected(transcoderSettings.isIoRedirected())
                .withExecutablePath(transcoderSettings.getTranscoderExecutable());
    }

    @Override
    public boolean transcode(Media candidate, Profile profile) {
        log.info("Starting transcoding process...");
        Optional<Integer> exitCode = prepareProcess(profile.getArguments()).start();
        log.info("Transcoding process completed.");
        return exitCode.isPresent() && exitCode.get() != 0;
    }

    @Override
    public void transcode(Media candidate, Profile profile, Consumer<Boolean> listener) {
        log.info("Starting transcoding process...");
        prepareProcess(profile.getArguments()).start(exitCode -> {
        });

    }
}
