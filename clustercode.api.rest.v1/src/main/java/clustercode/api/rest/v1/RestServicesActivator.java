package clustercode.api.rest.v1;

import clustercode.api.domain.Activator;
import clustercode.api.domain.ActivatorContext;
import clustercode.api.event.RxEventBus;
import clustercode.api.event.messages.StartupCompletedEvent;
import clustercode.api.rest.v1.rest.VersionApi;
import io.logz.guice.jersey.JerseyServer;
import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.XSlf4j;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;

@XSlf4j
public class RestServicesActivator implements Activator {

    private final JerseyServer jerseyServer;
    private final RxEventBus eventBus;
    private final List<Disposable> handlers = new LinkedList<>();

    @Inject
    RestServicesActivator(JerseyServer jerseyServer,
                          RxEventBus eventBus) {
        this.jerseyServer = jerseyServer;
        this.eventBus = eventBus;
    }

    @Override
    public void preActivate(ActivatorContext context) {

    }

    @Override
    public void activate(ActivatorContext context) {
        log.debug("Starting REST services...");
        try {
            jerseyServer.start();
            handlers.add(eventBus
                    .listenFor(StartupCompletedEvent.class)
                    .subscribe(VersionApi::setVersion));
        } catch (Exception e) {
            log.catching(e);
            log.error("Disabled the REST API.");
        }
    }

    @Override
    public void deactivate(ActivatorContext context) {
        handlers.forEach(Disposable::dispose);
        handlers.clear();
        try {
            jerseyServer.stop();
        } catch (Exception e) {
            log.catching(e);
            log.error("Could not properly stop REST API.");
        }
    }
}
