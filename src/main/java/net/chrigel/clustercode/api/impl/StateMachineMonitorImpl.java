package net.chrigel.clustercode.api.impl;

import lombok.Synchronized;
import net.chrigel.clustercode.api.StateMachineMonitor;
import net.chrigel.clustercode.statemachine.states.State;

public class StateMachineMonitorImpl implements StateMachineMonitor {

    private State currentState;

    @Synchronized
    @Override
    public void setCurrentState(State state) {
        this.currentState = state;
    }

    @Override
    public State getCurrentState() {
        return currentState;
    }
}
