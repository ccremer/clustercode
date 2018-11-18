package clustercode.impl.transcode;

import clustercode.api.config.converter.PathConverter;
import org.aeonbits.owner.Config;

import java.nio.file.Path;

public interface TranscoderConfig extends Config {

    /**
     * Gets the path to the temporary directory, which is needed during transcoding.
     *
     * @return the path to the dir.
     */
    @Key("CC_TRANSCODE_TEMP_DIR")
    @DefaultValue("/var/tmp/clustercode")
    @ConverterClass(PathConverter.class)
    Path temporary_dir();

    /**
     * Gets the default video extension with leading "." (e.g. ".mkv").
     *
     * @return the default extension, not null.
     */
    @Key("CC_TRANSCODE_DEFAULT_FORMAT")
    @DefaultValue(".mkv")
    String default_video_extension();

    @Key("CC_MEDIA_INPUT_DIR")
    @DefaultValue("/input")
    @ConverterClass(PathConverter.class)
    Path base_input_dir();
}
