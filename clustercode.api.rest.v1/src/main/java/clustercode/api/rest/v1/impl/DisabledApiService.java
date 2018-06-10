package clustercode.api.rest.v1.impl;

import clustercode.api.rest.v1.RestApiServices;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DisabledApiService implements RestApiServices {

    @Override
    public void start() {
        log.debug("REST services are disabled.");
    }
}
