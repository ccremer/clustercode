package clustercode.api.rest.v1;

import org.aeonbits.owner.Config;

public interface RestServiceConfig extends Config {

    String REST_API_CONTEXT_PATH = "/v1";

    @Key("CC_REST_API_ENABLED")
    @DefaultValue("true")
    boolean rest_enabled();

    @Key("CC_REST_API_PORT")
    @DefaultValue("7700")
    int rest_api_port();
}
