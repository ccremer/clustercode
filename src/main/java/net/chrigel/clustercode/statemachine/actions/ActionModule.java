package net.chrigel.clustercode.statemachine.actions;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import net.chrigel.clustercode.statemachine.Action;

public class ActionModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<Action> actionBinder = Multibinder.newSetBinder(binder(), Action.class);
        actionBinder.addBinding().to(InitializeAction.class);
        actionBinder.addBinding().to(ScanMediaAction.class);
        actionBinder.addBinding().to(SelectMediaAction.class);
        actionBinder.addBinding().to(SelectProfileAction.class);
        actionBinder.addBinding().to(AddTaskInClusterAction.class);
        actionBinder.addBinding().to(TranscodeAction.class);
        actionBinder.addBinding().to(RemoveTaskFromClusterAction.class);
        actionBinder.addBinding().to(CleanupAction.class);
        actionBinder.addBinding().to(LoggedAction.class);
    }
}
