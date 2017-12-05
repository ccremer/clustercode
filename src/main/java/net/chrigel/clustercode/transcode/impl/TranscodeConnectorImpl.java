package net.chrigel.clustercode.transcode.impl;

import net.chrigel.clustercode.event.Event;
import net.chrigel.clustercode.event.EventBus;
import net.chrigel.clustercode.transcode.TranscodeConnector;
import net.chrigel.clustercode.transcode.TranscodingService;
import net.chrigel.clustercode.transcode.messages.CancelTranscodeMessage;
import net.chrigel.clustercode.transcode.messages.TranscodeMessage;
import net.chrigel.clustercode.util.OptionalFunction;

import javax.inject.Inject;

public class TranscodeConnectorImpl implements TranscodeConnector {

    private final TranscodingService transcodingService;
    private final EventBus<TranscodeMessage> transcodeBus;

    @Inject
    TranscodeConnectorImpl(
        TranscodingService transcodingService,
        EventBus<TranscodeMessage> transcodeBus
    ){
        this.transcodingService = transcodingService;
        this.transcodeBus = transcodeBus;
    }

    @Inject
    @Override
    public void start() {
        transcodeBus.registerEventHandler(CancelTranscodeMessage.class,
            OptionalFunction.ofNullable(this::cancelTask));


    }

    private Boolean cancelTask(Event<CancelTranscodeMessage> event) {
        return transcodingService.cancelTranscode();
    }
}
