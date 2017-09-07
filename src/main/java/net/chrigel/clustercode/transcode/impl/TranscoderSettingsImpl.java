package net.chrigel.clustercode.transcode.impl;

import lombok.ToString;
import net.chrigel.clustercode.transcode.TranscoderSettings;
import net.chrigel.clustercode.util.FilesystemProvider;

import javax.inject.Inject;
import javax.inject.Named;
import java.nio.file.Path;
import java.util.Locale;

@ToString
class TranscoderSettingsImpl implements TranscoderSettings {

    private final boolean ioRedirected;
    private final Path executable;
    private final Path tempDir;
    private final String defaultExtension;
    private final Transcoder type;

    @Inject
    TranscoderSettingsImpl(@Named(TranscodeModule.TRANSCODE_CLI_KEY) String executable,
                           @Named(TranscodeModule.TRANSCODE_TEMPDIR_KEY) String tempDir,
                           @Named(TranscodeModule.TRANSCODE_IO_REDIRECTED_KEY) boolean ioRedirected,
                           @Named(TranscodeModule.TRANSCODE_DEFAULT_FORMAT_KEY) String defaultExtension,
                           @Named(TranscodeModule.TRANSCODE_TYPE) String type) {
        this.ioRedirected = ioRedirected;
        this.executable = FilesystemProvider.getInstance().getPath(executable);
        this.tempDir = FilesystemProvider.getInstance().getPath(tempDir);
        this.defaultExtension = defaultExtension;
        this.type = Transcoder.valueOf(type.toUpperCase(Locale.ENGLISH));
    }

    @Override
    public Path getTranscoderExecutable() {
        return executable;
    }

    @Override
    public boolean isIoRedirected() {
        return ioRedirected;
    }

    @Override
    public Path getTemporaryDir() {
        return tempDir;
    }

    @Override
    public String getDefaultVideoExtension() {
        return defaultExtension;
    }

    @Override
    public Transcoder getTranscoderType() {
        return type;
    }

}
