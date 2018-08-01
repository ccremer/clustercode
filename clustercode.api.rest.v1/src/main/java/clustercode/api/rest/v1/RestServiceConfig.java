package clustercode.api.rest.v1;

import clustercode.api.transcode.Transcoder;
import org.aeonbits.owner.Config;

public interface RestServiceConfig extends Config {

    /**
     * Gets the type of transcoder.
     *
     * @return the enum.
     */
    @Key("CC_TRANSCODE_TYPE")
    @DefaultValue("FFMPEG")
    Transcoder transcoder_type();

    @Key("CC_REST_API_ENABLED")
    @DefaultValue("true")
    boolean rest_enabled();

    @Key("CC_REST_API_PORT")
    @DefaultValue("7700")
    int rest_api_port();
}
