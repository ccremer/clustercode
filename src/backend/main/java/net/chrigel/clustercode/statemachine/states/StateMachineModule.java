package net.chrigel.clustercode.statemachine.states;

import com.google.inject.AbstractModule;
import net.chrigel.clustercode.statemachine.StateMachineService;

public class StateMachineModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(StateMachineService.class).to(StateController.class);
    }
}
