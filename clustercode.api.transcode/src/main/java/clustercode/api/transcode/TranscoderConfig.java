package clustercode.api.transcode;

import clustercode.api.config.PathConverter;
import org.aeonbits.owner.Config;

import java.nio.file.Path;

public interface TranscoderConfig extends Config {

    /**
     * Gets the path to the transcoder executable.
     *
     * @return the path to the executable, not null.
     */
    @Key("CC_TRANSCODE_CLI")
    @DefaultValue("/usr/bin/ffmpeg")
    @ConverterClass(PathConverter.class)
    Path getTranscoderExecutable();

    /**
     * Indicates whether the output of the subprocess should be included in this process' output stream (true) or not.
     *
     * @return
     */
    @Key("CC_TRANSCODE_IO_REDIRECTED")
    @DefaultValue("false")
    boolean isIoRedirected();

    /**
     * Gets the path to the temporary directory, which is needed during transcoding.
     *
     * @return the path to the dir.
     */
    @Key("CC_TRANSCODE_TEMP_DIR")
    @DefaultValue("/var/tmp/clustercode")
    Path getTemporaryDir();

    /**
     * Gets the default video extension with leading "." (e.g. ".mkv").
     *
     * @return the default extension, not null.
     */
    @Key("CC_TRANSCODE_DEFAULT_FORMAT")
    @DefaultValue(".mkv")
    String getDefaultVideoExtension();

    /**
     * Gets the type of transcoder.
     *
     * @return the enum.
     */
    @Key("CC_TRANSCODE_TYPE")
    @DefaultValue("FFMPEG")
    Transcoder getTranscoderType();

    @Key("CC_MEDIA_INPUT_DIR")
    @DefaultValue("/input")
    @ConverterClass(PathConverter.class)
    Path base_input_dir();
}
