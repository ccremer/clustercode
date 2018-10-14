package clustercode.impl.cleanup;

import clustercode.api.domain.Activator;
import clustercode.api.domain.ActivatorContext;
import clustercode.api.event.RxEventBus;
import clustercode.api.event.messages.TranscodeFinishedEvent;
import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class CleanupActivator implements Activator {

    private final RxEventBus eventBus;
    private final CleanupMessageHandler messageHandler;
    private final List<Disposable> handlers = new LinkedList<>();

    @Inject
    CleanupActivator(RxEventBus eventBus, CleanupMessageHandler messageHandler) {
        this.eventBus = eventBus;
        this.messageHandler = messageHandler;
    }

    @Override
    public void preActivate(ActivatorContext context) {
        log.debug("Activating cleanup services.");
        handlers.add(eventBus
                .listenFor(TranscodeFinishedEvent.class, messageHandler::onTranscodeFinished));
    }

    @Override
    public void activate(ActivatorContext context) {
    }

    @Override
    public void deactivate(ActivatorContext context) {
        log.debug("Deactivating cleanup services.");
        handlers.forEach(Disposable::dispose);
        handlers.clear();
    }
}
