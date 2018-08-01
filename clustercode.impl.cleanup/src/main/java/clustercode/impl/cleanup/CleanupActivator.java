package clustercode.impl.cleanup;

import clustercode.api.cleanup.CleanupService;
import clustercode.api.domain.Activator;
import clustercode.api.domain.ActivatorContext;
import clustercode.api.event.RxEventBus;
import clustercode.api.event.messages.TranscodeFinishedEvent;
import io.reactivex.disposables.Disposable;

import javax.inject.Inject;

public class CleanupActivator implements Activator {

    private final RxEventBus eventBus;
    private final CleanupService cleanupService;
    private Disposable transcodeFinishedHandler;

    @Inject
    CleanupActivator(RxEventBus eventBus, CleanupService cleanupService) {
        this.eventBus = eventBus;
        this.cleanupService = cleanupService;
    }

    @Override
    public void activate(ActivatorContext context) {
        transcodeFinishedHandler = eventBus.register(TranscodeFinishedEvent.class,
                cleanupService::performCleanup);
    }

    @Override
    public void deactivate(ActivatorContext context) {
        transcodeFinishedHandler.dispose();
    }
}
