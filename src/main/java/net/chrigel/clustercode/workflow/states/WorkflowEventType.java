package net.chrigel.clustercode.workflow.states;

public class WorkflowEventType {
    public static final WorkflowEventType FINISHED = new WorkflowEventType();
    public static final WorkflowEventType TIMEOUT = new WorkflowEventType();
    public static final WorkflowEventType NO_RESULT = new WorkflowEventType();
    public static final WorkflowEventType RESULT = new WorkflowEventType();

}
