package net.chrigel.clustercode.statemachine;

import net.chrigel.clustercode.statemachine.actions.*;
import net.chrigel.clustercode.statemachine.states.State;
import net.chrigel.clustercode.statemachine.states.StateEvent;
import net.chrigel.clustercode.util.UnsafeCastUtil;
import org.squirrelframework.foundation.fsm.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.impl.AbstractStateMachine;

import javax.inject.Inject;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StateController
        extends AbstractStateMachine<StateController, State, StateEvent, StateContext>
        implements StateMachineService {

    private Map<Class<? extends Action>, Action> actions;

    StateController() {
        // needed because of reflection (FSM framework).
    }

    @Inject
    StateController(Set<Action> actionSet) {
        this.actions = actionSet.stream().collect(Collectors.toMap(action -> action.getClass(), Function.identity()));
    }

    @Override
    public void initialize() {

        StateMachineBuilder<StateController, State, StateEvent, StateContext> builder =
                StateMachineBuilderFactory.create(
                        getClass(), State.class, StateEvent.class, StateContext.class, StateContext.class);

        StateContext context = new StateContext();

        // initialize
        builder.onEntry(State.INITIAL).perform(actionOf(InitializeAction.class));

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
        double minutes = 1d;
        builder.defineTimedState(State.WAIT, (long) (minutes * 60d * 1000d), 0, StateEvent.TIMEOUT, context)
                .addEntryAction(actionOf(LoggedAction.class)
                        .withStatement("Waiting {} minutes.", minutes)
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

        builder.newStateMachine(State.INITIAL).start(context);
    }

    private <A extends Action> A actionOf(Class<A> clazz) {
        return UnsafeCastUtil.cast(this.actions.get(clazz));
    }
}
