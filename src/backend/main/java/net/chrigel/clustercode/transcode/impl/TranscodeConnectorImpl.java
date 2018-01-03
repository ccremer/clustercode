package net.chrigel.clustercode.transcode.impl;

import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.event.RxEventBus;
import net.chrigel.clustercode.transcode.TranscodeTask;
import net.chrigel.clustercode.transcode.TranscodingService;
import net.chrigel.clustercode.transcode.messages.CancelTranscodeMessage;

import javax.inject.Inject;

@XSlf4j
public class TranscodeConnectorImpl {

    private final TranscodingService transcodingService;

    @Inject
    TranscodeConnectorImpl(
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
