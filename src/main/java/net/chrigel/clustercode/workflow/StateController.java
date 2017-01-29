package net.chrigel.clustercode.workflow;

import net.chrigel.clustercode.statemachine.StateContext;
import net.chrigel.clustercode.workflow.states.WorkflowEventType;
import net.chrigel.clustercode.workflow.states.WorkflowState;
import org.squirrelframework.foundation.fsm.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.impl.AbstractStateMachine;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.concurrent.atomic.AtomicReference;

public class StateController
        extends AbstractStateMachine<StateController, WorkflowState, WorkflowEventType, StateContext>
        implements StateMachineService {


    private final StateContext context;
    private final Provider<ScanMediaAction> scanMediaActionProvider;

    @Inject
    StateController(StateContext context,
                    Provider<ScanMediaAction> scanMediaActionProvider) {
        this.context = context;
        this.scanMediaActionProvider = scanMediaActionProvider;
    }

    @Override
    public void initialize() {

        StateMachineBuilder<StateController, WorkflowState, WorkflowEventType, StateContext> builder =
                StateMachineBuilderFactory.create(
                        getClass(), WorkflowState.class, WorkflowEventType.class, StateContext.class);

        AtomicReference<StateController> stateMachine = new AtomicReference<>();

        builder.externalTransition()
                .from(WorkflowState.INITIAL)
                .to(WorkflowState.SCAN_MEDIA)
                .on(WorkflowEventType.FINISHED);
        //builder.onEntry(WorkflowState.INITIAL).perform(new InitializeAction());


        builder.onEntry(WorkflowState.SCAN_MEDIA)
                .perform(scanMediaActionProvider.get()
                        .withListener(result -> {
                            if (result.isPresent()) {
                                stateMachine.get().fire(WorkflowEventType.RESULT, context);
                            } else {
                                stateMachine.get().fire(WorkflowEventType.NO_RESULT, context);
                            }
                        }));


        // Fire event after 10 minutes.
        builder.defineTimedState(WorkflowState.WAIT, 600000, 0, WorkflowEventType.TIMEOUT, context);
        builder.externalTransition()
                .from(WorkflowState.WAIT)
                .to(WorkflowState.SCAN_MEDIA)
                .on(WorkflowEventType.TIMEOUT);

        builder.externalTransition()
                .from(WorkflowState.SCAN_MEDIA)
                .to(WorkflowState.WAIT)
                .on(WorkflowEventType.NO_RESULT);

        builder.externalTransition()
                .from(WorkflowState.SCAN_MEDIA)
                .to(WorkflowState.SELECT_MEDIA)
                .on(WorkflowEventType.RESULT);

        builder.externalTransition()
                .from(WorkflowState.SELECT_MEDIA)
                .to(WorkflowState.PARSE_PROFILE)
                .on(WorkflowEventType.RESULT);

        builder.externalTransition()
                .from(WorkflowState.WAIT)
                .to(WorkflowState.PARSE_PROFILE)
                .on(WorkflowEventType.NO_RESULT);

        builder.externalTransition()
                .from(WorkflowState.PARSE_PROFILE)
                .to(WorkflowState.TRANSCODE)
                .on(WorkflowEventType.RESULT);

        builder.externalTransition()
                .from(WorkflowState.TRANSCODE)
                .to(WorkflowState.CLEANUP)
                .on(WorkflowEventType.FINISHED);

        builder.externalTransition()
                .from(WorkflowState.CLEANUP)
                .to(WorkflowState.SCAN_MEDIA)
                .on(WorkflowEventType.FINISHED);

        stateMachine.set(builder.newStateMachine(WorkflowState.INITIAL));

        stateMachine.get().start();
    }

}
