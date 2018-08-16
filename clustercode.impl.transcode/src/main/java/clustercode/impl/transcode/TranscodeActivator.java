package clustercode.impl.transcode;

import clustercode.api.domain.Activator;
import clustercode.api.domain.ActivatorContext;
import clustercode.api.domain.TranscodeTask;
import clustercode.api.event.RxEventBus;
import clustercode.api.event.messages.CancelTranscodeMessage;
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
    private final List<Disposable> handlers = new LinkedList<>();

    @Inject
    TranscodeActivator(
            TranscodingService transcodingService,
            RxEventBus eventBus
    ) {
        this.transcodingService = transcodingService;
        this.eventBus = eventBus;
    }

    @Inject
    @Override
    public void activate(ActivatorContext context) {
        eventBus.listenFor(CancelTranscodeMessage.class, this::onCancelTranscodeTask);
        eventBus.listenFor(TranscodeTask.class, transcodingService::transcode);

        handlers.add(transcodingService
                .onProgressUpdated()
                .subscribe(eventBus::emit));
        handlers.add(transcodingService
                .onTranscodeBegin()
                .subscribe(eventBus::emit));
        handlers.add(transcodingService
                .onTranscodeFinished()
                .subscribe(eventBus::emit));
    }

    private void onCancelTranscodeTask(CancelTranscodeMessage event) {
        event.setCancelled(transcodingService.cancelTranscode());
    }

    @Override
    public void deactivate(ActivatorContext context) {
        handlers.forEach(Disposable::dispose);
        handlers.clear();
    }
}
