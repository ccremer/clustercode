package clustercode.impl.transcode;

import clustercode.api.domain.TranscodeTask;
import clustercode.api.event.RxEventBus;
import clustercode.api.event.messages.CancelTranscodeMessage;
import clustercode.api.transcode.TranscodingService;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@Slf4j
public class TranscodeActivator {

    private final TranscodingService transcodingService;

    @Inject
    TranscodeActivator(
        TranscodingService transcodingService,
        RxEventBus eventBus
    ) {
        this.transcodingService = transcodingService;
        eventBus.register(CancelTranscodeMessage.class, this::onCancelTranscodeTask);
        eventBus.register(TranscodeTask.class, transcodingService::transcode);

        transcodingService.onProgressUpdated()
                          .subscribe(eventBus::emit);
        transcodingService.onTranscodeBegin()
                          .subscribe(eventBus::emit);
        transcodingService.onTranscodeFinished()
                          .subscribe(eventBus::emit);
    }

    private void onCancelTranscodeTask(CancelTranscodeMessage event) {
        event.setCancelled(transcodingService.cancelTranscode());
    }

}
