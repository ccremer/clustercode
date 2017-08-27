package net.chrigel.clustercode.api;

import net.chrigel.clustercode.statemachine.states.State;

public interface StateMachineMonitor {

    void setCurrentState(State state);

    State getCurrentState();

}
