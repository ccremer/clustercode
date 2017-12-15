package net.chrigel.clustercode.transcode.impl;

import net.chrigel.clustercode.event.Event;
import net.chrigel.clustercode.event.EventBus;
import net.chrigel.clustercode.transcode.TranscodeConnector;
import net.chrigel.clustercode.transcode.TranscodingService;
import net.chrigel.clustercode.transcode.messages.CancelTranscodeMessage;
import net.chrigel.clustercode.transcode.messages.ProgressMessage;
import net.chrigel.clustercode.transcode.messages.TranscodeMessage;
import net.chrigel.clustercode.util.OptionalFunction;

import javax.inject.Inject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TranscodeConnectorImpl implements TranscodeConnector {

    private final TranscodingService transcodingService;
    private final EventBus<TranscodeMessage> transcodeBus;

    @Inject
    TranscodeConnectorImpl(
        TranscodingService transcodingService,
        EventBus<TranscodeMessage> transcodeBus
    ) {
        this.transcodingService = transcodingService;
        this.transcodeBus = transcodeBus;
    }

    @Inject
    @Override
    public void start() {
        transcodeBus.registerEventHandler(CancelTranscodeMessage.class, this::onCancelTranscodeTask);

        startProgressUpdateLoop();
    }

    private void onCancelTranscodeTask(Event<CancelTranscodeMessage> event) {
        event.addAnswer(transcodingService.cancelTranscode());
    }

    private void startProgressUpdateLoop() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() ->
            transcodingService.getProgressCalculator().getProgress().ifPresent(result ->
                transcodeBus.emit(ProgressMessage.builder()
                                                 .percentage(result.getPercentage())
                                                 .build())
            ), 10, 10, TimeUnit.SECONDS);
    }
}
