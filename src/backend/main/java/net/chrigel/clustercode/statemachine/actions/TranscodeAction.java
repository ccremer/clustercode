package net.chrigel.clustercode.statemachine.actions;

import net.chrigel.clustercode.event.RxEventBus;
import net.chrigel.clustercode.statemachine.Action;
import net.chrigel.clustercode.statemachine.StateContext;
import net.chrigel.clustercode.statemachine.states.State;
import net.chrigel.clustercode.statemachine.states.StateEvent;
import net.chrigel.clustercode.transcode.TranscodeResult;
import net.chrigel.clustercode.transcode.TranscodeTask;
import net.chrigel.clustercode.transcode.TranscodingService;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class TranscodeAction extends Action {

    private final RxEventBus eventBus;
    private CompletableFuture<TranscodeResult> future;

    @Inject
    TranscodeAction(RxEventBus eventBus) {
        this.eventBus = eventBus;

        eventBus.register(TranscodeResult.class, this::onTranscodeFinished);
    }

    private void onTranscodeFinished(TranscodeResult event) {
        future.complete(event);
    }

    @Override
    protected StateEvent doExecute(State from, State to, StateEvent event, StateContext context) {
        log.entry(from, to, event, context);

        future = new CompletableFuture<>();
        eventBus.emit(TranscodeTask
            .builder()
            .media(context.getSelectedMedia())
            .profile(context.getSelectedProfile())
            .build()
        );

        try {
            TranscodeResult result = future.get();
            context.setTranscodeResult(result);
            return result.isCancelled() ? StateEvent.CANCELLED : StateEvent.FINISHED;
        } catch (InterruptedException | ExecutionException e) {
            return StateEvent.ERROR;
        }
    }

}
