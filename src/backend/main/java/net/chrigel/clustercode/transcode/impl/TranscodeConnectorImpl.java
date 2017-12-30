package net.chrigel.clustercode.transcode.impl;

import net.chrigel.clustercode.event.RxEventBus;
import net.chrigel.clustercode.transcode.TranscodeTask;
import net.chrigel.clustercode.transcode.TranscodingService;
import net.chrigel.clustercode.transcode.messages.CancelTranscodeMessage;

import javax.inject.Inject;

public class TranscodeConnectorImpl {

    private final TranscodingService transcodingService;

    @Inject
    TranscodeConnectorImpl(
        TranscodingService transcodingService,
        RxEventBus eventBus
    ) {
        this.transcodingService = transcodingService;
        eventBus.register(CancelTranscodeMessage.class, this::onCancelTranscodeTask);
    }

    private void onCancelTranscodeTask(CancelTranscodeMessage event) {
        event.setCancelled(transcodingService.cancelTranscode());
    }

}
