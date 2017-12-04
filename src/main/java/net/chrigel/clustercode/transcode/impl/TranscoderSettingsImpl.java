package net.chrigel.clustercode.transcode.impl;

import com.google.inject.Inject;
import lombok.Getter;
import lombok.ToString;
import net.chrigel.clustercode.transcode.TranscoderSettings;
import net.chrigel.clustercode.util.FilesystemProvider;

import javax.inject.Named;
import java.nio.file.Path;
import java.util.Locale;

@ToString
@Getter
class TranscoderSettingsImpl implements TranscoderSettings {

    private boolean isIoRedirected;
    private Path transcoderExecutable;
    private Path tempDir;
    private String defaultVideoExtension = ".mkv";
    private Transcoder transcoderType = Transcoder.FFMPEG;

    TranscoderSettingsImpl() {
        setExecutable("/usr/bin/ffmpeg");
        setTempDir("/var/tmp/clustercode");
    }

    @Inject(optional = true)
    void setTranscoderType(@Named(TranscodeModule.TRANSCODE_TYPE_KEY) String transcoderType) {
        this.transcoderType = Transcoder.valueOf(transcoderType.toUpperCase(Locale.ENGLISH));
    }

    @Inject(optional = true)
    void setExecutable(@Named(TranscodeModule.TRANSCODE_CLI_KEY) String executable) {
        this.transcoderExecutable = FilesystemProvider.getInstance().getPath(executable);
    }

    @Inject(optional = true)
    void setIoRedirected(@Named(TranscodeModule.TRANSCODE_IO_REDIRECTED_KEY) boolean isIoRedirected) {
        this.isIoRedirected = isIoRedirected;
    }

    @Inject(optional = true)
    void setDefaultVideoExtension(@Named(TranscodeModule.TRANSCODE_DEFAULT_FORMAT_KEY) String defaultVideoExtension) {
        this.defaultVideoExtension = defaultVideoExtension;
    }

    @Inject(optional = true)
    void setTempDir(@Named(TranscodeModule.TRANSCODE_TEMPDIR_KEY) String tempDir) {
        this.tempDir = FilesystemProvider.getInstance().getPath(tempDir);
    }

    @Override
    public Path getTemporaryDir() {
        return tempDir;
    }

}
