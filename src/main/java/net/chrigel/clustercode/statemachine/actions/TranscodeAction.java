package net.chrigel.clustercode.statemachine.actions;

import net.chrigel.clustercode.cluster.ClusterService;
import net.chrigel.clustercode.scan.Media;
import net.chrigel.clustercode.statemachine.StateContext;
import net.chrigel.clustercode.transcode.TranscodeTask;
import net.chrigel.clustercode.transcode.TranscodingService;
import net.chrigel.clustercode.statemachine.AbstractAction;
import net.chrigel.clustercode.statemachine.states.State;
import net.chrigel.clustercode.statemachine.states.StateEvent;

import javax.inject.Inject;

public class TranscodeAction extends AbstractAction {

    private final TranscodingService transcodingService;
    private ClusterService clusterService;

    @Inject
    TranscodeAction(TranscodingService transcodingService,
                    ClusterService clusterService) {
        this.transcodingService = transcodingService;
        this.clusterService = clusterService;
    }

    @Override
    protected StateEvent doExecute(State from, State to, StateEvent event, StateContext context) {
        log.entry(from, to, event, context);
        Media candidate = context.getSelectedMedia();
        clusterService.setTask(candidate);
        context.setTranscodeResult(transcodingService.transcode(TranscodeTask.builder()
                .media(candidate)
                .profile(context.getSelectedProfile())
                .build()));
        clusterService.removeTask();
        return StateEvent.FINISHED;
    }
}
