package net.chrigel.clustercode.workflow.states;

public enum WorkflowState {

    INITIAL,
    SCAN_MEDIA,
    WAIT,
    SELECT_MEDIA,
    PARSE_PROFILE,
    TRANSCODE,
    CLEANUP,

}
