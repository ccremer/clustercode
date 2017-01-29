package net.chrigel.clustercode.transcode.impl;

import net.chrigel.clustercode.process.ExternalProcess;
import net.chrigel.clustercode.transcode.Transcoder;
import net.chrigel.clustercode.transcode.TranscoderSettings;

import javax.inject.Inject;
import javax.inject.Provider;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class TranscoderImpl implements Transcoder {

    public static final String INPUT_PLACEHOLDER = "${INPUT}";
    public static final String OUTPUT_PLACEHOLDER = "${OUTPUT}";

    private final Provider<ExternalProcess> processProvider;
    private final TranscoderSettings settings;
    private Path source;
    private Path target;
    private Stream<String> arguments;

    @Inject
    TranscoderImpl(Provider<ExternalProcess> processProvider,
                   TranscoderSettings settings) {
        this.processProvider = processProvider;
        this.settings = settings;
    }

    private String replaceOutput(String s) {
        return s.replace(OUTPUT_PLACEHOLDER, target.toString());
    }

    private String replaceInput(String s) {
        return s.replace(INPUT_PLACEHOLDER, source.toString());
    }

    @Override
    public final Transcoder from(Path source) {
        this.source = source;
        return this;
    }

    @Override
    public final Transcoder to(Path output) {
        this.target = output;
        return this;
    }

    @Override
    public final Transcoder withArguments(Stream<String> arguments) {
        this.arguments = arguments;
        return this;
    }

    @Override
    public final boolean transcode() {
        Optional<Integer> exitCode = processProvider.get()
                .withExecutablePath(settings.getTranscoderExecutable())
                .withIORedirected(settings.isIoRedirected())
                .withArguments(prepareArguments())
                .start();
        return exitCode.isPresent() && exitCode.get() == 0;
    }

    List<String> prepareArguments() {
        return arguments
                .map(this::replaceInput)
                .map(this::replaceOutput)
                .collect(Collectors.toList());
    }

}
