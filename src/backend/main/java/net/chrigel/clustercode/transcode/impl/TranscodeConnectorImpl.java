package net.chrigel.clustercode.transcode.impl;

import net.chrigel.clustercode.event.RxEventBus;
import net.chrigel.clustercode.transcode.TranscodeTask;
import net.chrigel.clustercode.transcode.TranscodingService;
import net.chrigel.clustercode.transcode.messages.CancelTranscodeMessage;

import javax.inject.Inject;

public class TranscodeConnectorImpl {

    private final TranscodingService transcodingService;
    private final RxEventBus eventBus;

    @Inject
    TranscodeConnectorImpl(
        TranscodingService transcodingService,
        RxEventBus eventBus
    ) {
        this.transcodingService = transcodingService;
        this.eventBus = eventBus;
        eventBus.register(CancelTranscodeMessage.class, this::onCancelTranscodeTask);
        eventBus.register(TranscodeTask.class, this::onTranscodeTaskCreated);
    }

    private void onTranscodeTaskCreated(TranscodeTask event) {
        transcodingService.transcode(event, eventBus::emit);
        event.setAccepted(!transcodingService.isActive());
    }

    private void onCancelTranscodeTask(CancelTranscodeMessage event) {
        event.setCancelled(transcodingService.cancelTranscode());
    }

}
