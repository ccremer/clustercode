package net.chrigel.clustercode.statemachine.states;

public enum StateEvent {
    FINISHED,
    TIMEOUT,
    NO_RESULT,
    RESULT,
    ERROR,
    CANCELLED;

    @Override
    public String toString() {
        return getClass().getSimpleName().concat(".").concat(super.toString());
    }
}
