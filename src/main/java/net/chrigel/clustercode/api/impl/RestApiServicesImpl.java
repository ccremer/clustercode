package net.chrigel.clustercode.api.impl;

import io.logz.guice.jersey.JerseyServer;
import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.api.RestApiServices;

import javax.inject.Inject;

@XSlf4j
public class RestApiServicesImpl implements RestApiServices {

    private final JerseyServer jerseyServer;

    @Inject
    RestApiServicesImpl(JerseyServer jerseyServer) {

        this.jerseyServer = jerseyServer;
    }

    @Override
    public void start() {
        log.info("Starting REST services...");
        try {
            jerseyServer.start();
        } catch (Exception e) {
            log.catching(e);
        }
    }
}
