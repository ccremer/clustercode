package net.chrigel.clustercode.api.impl;

import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.api.RestApiServices;

@XSlf4j
public class DisabledApiService implements RestApiServices {

    @Override
    public void start() {
        log.debug("REST services are disabled.");
    }
}
