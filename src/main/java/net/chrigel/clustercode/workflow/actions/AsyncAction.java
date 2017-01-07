package net.chrigel.clustercode.workflow.actions;

import net.chrigel.clustercode.statemachine.StateContext;
import net.chrigel.clustercode.workflow.StateController;
import net.chrigel.clustercode.workflow.states.WorkflowEventType;
import net.chrigel.clustercode.workflow.states.WorkflowState;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class AsyncAction<T> extends AbstractAction {

    private Optional<Consumer<Optional<T>>> listener = Optional.empty();

    /**
     * Configures the action to call the given listener when it completes. This overrides
     * {@link #withListener(Runnable)}.
     *
     * @param listener the listener which gets the result of the action after it completes asynchronously.
     * @return this.
     */
    public final AsyncAction<T> withListener(Consumer<Optional<T>> listener) {
        this.listener = Optional.of(listener);
        return this;
    }

    /**
     * Configures the action to call the given listener when it completes. This overrides
     * {@link #withListener(Consumer)}.
     *
     * @param listener the listener which is not interested in the result.
     * @return this.
     */
    public final AsyncAction<T> withListener(Runnable listener) {
        Objects.requireNonNull(listener);
        this.listener = Optional.of(t -> listener.run());
        return this;
    }


    @Override
    public final void execute(WorkflowState from, WorkflowState to, WorkflowEventType event, StateContext context,
                              StateController stateMachine) {
        CompletableFuture.runAsync(() -> {
            Optional<T> result = doExecute(from, to, event, context);
            listener.ifPresent(listener -> listener.accept(result));
        });
    }

    /**
     * This method is being executed asynchronously. After returning the result, the listener will be notified.
     *
     * @param from
     * @param to
     * @param event   the firing event type.
     * @param context the state machine context
     * @return the result of the action. Returns empty if no result required or available.
     */
    protected abstract Optional<T> doExecute(WorkflowState from, WorkflowState to, WorkflowEventType
            event, StateContext context);

}
