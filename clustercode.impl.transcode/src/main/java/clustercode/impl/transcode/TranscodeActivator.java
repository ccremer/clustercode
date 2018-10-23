package clustercode.impl.transcode;

import clustercode.api.domain.Activator;
import clustercode.api.domain.ActivatorContext;
import clustercode.api.domain.TranscodeTask;
import clustercode.api.event.RxEventBus;
import clustercode.api.event.messages.CancelTranscodeMessage;
import clustercode.api.event.messages.ProfileSelectedMessage;
import clustercode.api.transcode.TranscodingService;
import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class TranscodeActivator implements Activator {

    private final TranscodingService transcodingService;
    private final RxEventBus eventBus;
    private final TranscodingMessageHandler messageHandler;
    private final List<Disposable> handlers = new LinkedList<>();

    @Inject
    TranscodeActivator(
            TranscodingService transcodingService,
            RxEventBus eventBus,
            TranscodingMessageHandler messageHandler
    ) {
        this.transcodingService = transcodingService;
        this.eventBus = eventBus;
        this.messageHandler = messageHandler;
    }

    @Override
    public void preActivate(ActivatorContext context) {
        log.debug("Activating transcoding services.");
        handlers.add(eventBus
                .listenFor(CancelTranscodeMessage.class, this::onCancelTranscodeTask));
        handlers.add(eventBus
                .listenFor(TranscodeTask.class, transcodingService::transcode));
        transcodingService
                .onProgressUpdated(eventBus::emit)
                .onTranscodeBegin(eventBus::emit)
                .onTranscodeFinished(eventBus::emit);

        handlers.add(eventBus
                .listenFor(ProfileSelectedMessage.class)
                .filter(ProfileSelectedMessage::isSelected)
                .subscribe(messageHandler::onProfileSelected));
    }

    @Override
    public void activate(ActivatorContext context) {

    }

    private void onCancelTranscodeTask(CancelTranscodeMessage event) {
        event.setCancelled(transcodingService.cancelTranscode());
    }

    @Override
    public void deactivate(ActivatorContext context) {
        log.debug("Deactivating transcoding services.");
        handlers.forEach(Disposable::dispose);
        handlers.clear();
    }
}
