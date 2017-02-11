package net.chrigel.clustercode.statemachine;

import net.chrigel.clustercode.statemachine.actions.*;
import net.chrigel.clustercode.statemachine.states.State;
import net.chrigel.clustercode.statemachine.states.StateEvent;
import org.squirrelframework.foundation.fsm.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.impl.AbstractStateMachine;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicReference;

public class StateController
        extends AbstractStateMachine<StateController, State, StateEvent, StateContext>
        implements StateMachineService {


    private StateContext context;
    private ScanMediaAction scanMediaAction;
    private InitializeAction initializeAction;
    private SelectMediaAction selectMediaAction;
    private ParseProfileAction parseProfileAction;
    private TranscodeAction transcodeAction;
    private CleanupAction cleanupAction;

    StateController() {
        // needed because of reflection (FSM framework).
    }

    @Inject
    StateController(ScanMediaAction scanMediaAction,
                    InitializeAction initializeAction,
                    SelectMediaAction selectMediaAction,
                    ParseProfileAction parseProfileAction,
                    TranscodeAction transcodeAction,
                    CleanupAction cleanupAction) {
        this.scanMediaAction = scanMediaAction;
        this.initializeAction = initializeAction;
        this.selectMediaAction = selectMediaAction;
        this.parseProfileAction = parseProfileAction;
        this.transcodeAction = transcodeAction;
        this.cleanupAction = cleanupAction;
    }

    @Override
    public void initialize() {

        StateMachineBuilder<StateController, State, StateEvent, StateContext> builder =
                StateMachineBuilderFactory.create(
                        getClass(), State.class, StateEvent.class, StateContext.class, StateContext.class);

        this.context = new StateContext();
        AtomicReference<StateController> stateMachine = new AtomicReference<>();


        // initialize
        builder.onEntry(State.INITIAL).perform(initializeAction);
        builder.externalTransition()
                .from(State.INITIAL)
                .to(State.SCAN_MEDIA)
                .on(StateEvent.FINISHED);

        // scan
        builder.onEntry(State.SCAN_MEDIA).perform(scanMediaAction);

        // select media if found...
        builder.externalTransition()
                .from(State.SCAN_MEDIA)
                .to(State.SELECT_MEDIA)
                .on(StateEvent.RESULT);
        builder.onEntry(State.SELECT_MEDIA).perform(selectMediaAction);

        // Fire event after some minutes on empty result.
        double minutes = 1d;
        builder.defineTimedState(State.WAIT, (long) (minutes * 60d * 1000d), 0, StateEvent.TIMEOUT, context)
                .addEntryAction(new LoggedAction("Waiting {} minutes.", minutes).withName(getClass()));

        // wait if not
        builder.externalTransition()
                .from(State.SCAN_MEDIA)
                .to(State.WAIT)
                .on(StateEvent.NO_RESULT);

        // after waiting, back to scanning
        builder.externalTransition()
                .from(State.WAIT)
                .to(State.SCAN_MEDIA)
                .on(StateEvent.TIMEOUT);

        // when selected, parse the selectedProfile
        builder.externalTransition()
                .from(State.SELECT_MEDIA)
                .to(State.PARSE_PROFILE)
                .on(StateEvent.RESULT);
        builder.onEntry(State.PARSE_PROFILE).perform(parseProfileAction);

        // if no selectedProfile selectable, go to waiting.
        builder.externalTransition()
                .from(State.PARSE_PROFILE)
                .to(State.WAIT)
                .on(StateEvent.NO_RESULT);

        // after selectedProfile parsing, go to transcoding
        builder.externalTransition()
                .from(State.PARSE_PROFILE)
                .to(State.TRANSCODE)
                .on(StateEvent.RESULT);
        builder.onEntry(State.TRANSCODE).perform(transcodeAction);

        // transcoding -> Cleanup
        builder.externalTransition()
                .from(State.TRANSCODE)
                .to(State.CLEANUP)
                .on(StateEvent.FINISHED);
        builder.onEntry(State.CLEANUP).perform(cleanupAction);

        // cleanup -> scanning
        builder.externalTransition()
                .from(State.CLEANUP)
                .to(State.SCAN_MEDIA)
                .on(StateEvent.FINISHED);

        stateMachine.set(builder.newStateMachine(State.INITIAL));

        stateMachine.get().start();
    }

    public StateContext getStateContext() {
        return context;
    }

}
