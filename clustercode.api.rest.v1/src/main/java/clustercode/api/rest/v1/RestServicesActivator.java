package clustercode.api.rest.v1;

import clustercode.api.domain.Activator;
import clustercode.api.domain.ActivatorContext;
import io.logz.guice.jersey.JerseyServer;
import lombok.extern.slf4j.XSlf4j;

import javax.inject.Inject;

@XSlf4j
public class RestServicesActivator implements Activator {


    public static final String REST_API_CONTEXT_PATH = "/v1";
    private final JerseyServer jerseyServer;

    @Inject
    RestServicesActivator(JerseyServer jerseyServer) {
        this.jerseyServer = jerseyServer;
    }

    @Inject
    @Override
    public void activate(ActivatorContext context) {
        log.info("Starting REST services...");
        try {
            jerseyServer.start();
        } catch (Exception e) {
            log.catching(e);
            log.error("Disabled the REST API.");
        }
    }

    @Override
    public void deactivate(ActivatorContext context) {
        try {
            jerseyServer.stop();
        } catch (Exception e) {
            log.catching(e);
            log.error("Could not properly stop REST API.");
        }
    }
}
