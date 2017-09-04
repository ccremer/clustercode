package net.chrigel.clustercode.statemachine.actions;

import net.chrigel.clustercode.cluster.ClusterService;
import net.chrigel.clustercode.statemachine.Action;
import net.chrigel.clustercode.statemachine.StateContext;
import net.chrigel.clustercode.statemachine.states.State;
import net.chrigel.clustercode.statemachine.states.StateEvent;
import net.chrigel.clustercode.transcode.TranscodeTask;
import net.chrigel.clustercode.transcode.TranscodingService;

import javax.inject.Inject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TranscodeAction extends Action {

    private final TranscodingService transcodingService;
    private final ClusterService clusterService;
    private ScheduledExecutorService executor;

    @Inject
    TranscodeAction(TranscodingService transcodingService,
                    ClusterService clusterService) {
        this.transcodingService = transcodingService;
        this.clusterService = clusterService;

    }

    @Override
    protected StateEvent doExecute(State from, State to, StateEvent event, StateContext context) {
        log.entry(from, to, event, context);
        if (this.executor != null) executor.shutdown();
        startProgressUpdateLoop();
        context.setTranscodeResult(transcodingService.transcode(TranscodeTask.builder()
            .media(context.getSelectedMedia())
            .profile(context.getSelectedProfile())
            .build()));
        executor.shutdown();
        return StateEvent.FINISHED;
    }

    private void startProgressUpdateLoop() {
        this.executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() ->
            transcodingService.getProgressCalculator().getProgress().ifPresent(result ->
                clusterService.setProgress(result.getPercentage())), 10, 10, TimeUnit.SECONDS);
    }
}
