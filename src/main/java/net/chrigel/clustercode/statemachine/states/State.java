package net.chrigel.clustercode.statemachine.states;

public enum State {

    INITIAL,
    SCAN_MEDIA,
    WAIT,
    SELECT_MEDIA,
    SELECT_PROFILE,
    TRANSCODE,
    CLEANUP,;

    @Override
    public String toString() {
        return getClass().getSimpleName().concat(".").concat(super.toString());
    }
}
