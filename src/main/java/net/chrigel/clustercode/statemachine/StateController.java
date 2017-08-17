package net.chrigel.clustercode.statemachine;

import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.cluster.ClusterSettings;
import net.chrigel.clustercode.scan.MediaScanSettings;
import net.chrigel.clustercode.statemachine.actions.*;
import net.chrigel.clustercode.statemachine.states.State;
import net.chrigel.clustercode.statemachine.states.StateEvent;
import org.squirrelframework.foundation.fsm.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.impl.AbstractStateMachine;

import javax.inject.Inject;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@XSlf4j
public class StateController
        extends AbstractStateMachine<StateController, State, StateEvent, StateContext>
        implements StateMachineService {

    private long scanInterval;
    private boolean isArbiter;
    private Map<Class<? extends Action>, Action> actions;

    @SuppressWarnings("unused")
    StateController() {
        // needed because of reflection (FSM framework).
    }

    @SuppressWarnings("unused")
    @Inject
    StateController(Set<Action> actionSet,
                    MediaScanSettings scanSettings,
                    ClusterSettings clusterSettings) {
        this.actions = actionSet.stream().collect(Collectors.toMap(action -> action.getClass(), Function.identity()));
        this.isArbiter = clusterSettings.isArbiter();
        this.scanInterval = scanSettings.getMediaScanInterval();
    }

    @Override
    public void initialize() {

        StateMachineBuilder<StateController, State, StateEvent, StateContext> builder =
                StateMachineBuilderFactory.create(
                        getClass(), State.class, StateEvent.class, StateContext.class, StateContext.class);

        StateContext context = new StateContext();

        // initialize
        builder.onEntry(State.INITIAL).perform(actionOf(InitializeAction.class));

        if (isArbiter) {
            log.info("Configuring local node as arbiter cluster member.");
        } else {
            log.info("Configuring local node as active cluster member.");

            // initial -------->> scanning
            builder.onEntry(State.SCAN_MEDIA).perform(actionOf(ScanMediaAction.class));
            builder.externalTransition()
                    .from(State.INITIAL)
                    .to(State.SCAN_MEDIA)
                    .on(StateEvent.FINISHED);

            // scanning ------->> select media
            builder.externalTransition()
                    .from(State.SCAN_MEDIA)
                    .to(State.SELECT_MEDIA)
                    .on(StateEvent.RESULT);
            builder.onEntry(State.SELECT_MEDIA).perform(actionOf(SelectMediaAction.class));

            // Fire event after some minutes on empty result.
            builder.defineTimedState(State.WAIT, scanInterval * 60000, 0, StateEvent.TIMEOUT, context)
                    .addEntryAction(actionOf(LoggedAction.class)
                            .withStatement("Waiting {} minutes.", scanInterval)
                            .withName(getClass()));

            // scanning ------->> waiting
            builder.externalTransition()
                    .from(State.SCAN_MEDIA)
                    .to(State.WAIT)
                    .on(StateEvent.NO_RESULT);

            // waiting ----->> scanning
            builder.externalTransition()
                    .from(State.WAIT)
                    .to(State.SCAN_MEDIA)
                    .on(StateEvent.TIMEOUT);

            // fail when select media ------>> waiting
            builder.externalTransition()
                    .from(State.SELECT_MEDIA)
                    .to(State.WAIT)
                    .on(StateEvent.NO_RESULT);

            // media selected ------>> select profile
            builder.externalTransition()
                    .from(State.SELECT_MEDIA)
                    .to(State.SELECT_PROFILE)
                    .on(StateEvent.RESULT);
            builder.onEntry(State.SELECT_PROFILE).perform(actionOf(SelectProfileAction.class));

            // select profile ------->> waiting.
            builder.externalTransition()
                    .from(State.SELECT_PROFILE)
                    .to(State.WAIT)
                    .on(StateEvent.NO_RESULT);

            // select profile ------->> transcoding
            builder.externalTransition()
                    .from(State.SELECT_PROFILE)
                    .to(State.TRANSCODE)
                    .on(StateEvent.RESULT)
                    .perform(actionOf(AddTaskInClusterAction.class));
            builder.onEntry(State.TRANSCODE).perform(actionOf(TranscodeAction.class));

            // transcoding ------->> Cleanup
            builder.externalTransition()
                    .from(State.TRANSCODE)
                    .to(State.CLEANUP)
                    .on(StateEvent.FINISHED)
                    .perform(actionOf(RemoveTaskFromClusterAction.class));
            builder.onEntry(State.CLEANUP).perform(actionOf(CleanupAction.class));

            // cleanup ------->> scanning
            builder.externalTransition()
                    .from(State.CLEANUP)
                    .to(State.SCAN_MEDIA)
                    .on(StateEvent.FINISHED);
        }
        builder.newStateMachine(State.INITIAL).start(context);
    }

    private <A extends Action> A actionOf(Class<A> clazz) {
        return (A) this.actions.get(clazz);
    }
}
