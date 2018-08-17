package clustercode.impl.cleanup;

import clustercode.api.cleanup.CleanupService;
import clustercode.api.event.RxEventBus;
import clustercode.api.event.messages.CleanupFinishedMessage;
import clustercode.api.event.messages.TranscodeFinishedEvent;

import javax.inject.Inject;

class CleanupMessageHandler {

    private final RxEventBus eventBus;
    private final CleanupService cleanupService;

    @Inject
    CleanupMessageHandler(
            RxEventBus eventBus,
                          CleanupService cleanupService) {
        this.eventBus = eventBus;
        this.cleanupService = cleanupService;
    }

    void onTranscodeFinished(TranscodeFinishedEvent msg) {
        cleanupService.performCleanup(msg);
        eventBus.emitAsync(new CleanupFinishedMessage());
    }

}
