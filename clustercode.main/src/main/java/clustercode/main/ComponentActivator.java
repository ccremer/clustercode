package clustercode.main;

import clustercode.api.domain.Activator;
import clustercode.api.domain.ActivatorContext;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

class ComponentActivator {

    private final Set<Activator> activators;
    private final ActivatorContext context;

    @Inject
    ComponentActivator(
            Set<Activator> activators,
            ActivatorContext context
    ) {
        this.context = context;
        this.activators = new HashSet<>(activators);
    }

    void preActivateServices() {
        this.activators.forEach(activator -> activator.preActivate(context));
    }

    void activateServices() {
        this.activators.forEach(activator -> activator.activate(context));
    }

    void deactivateServices() {
        this.activators.forEach(activator -> activator.deactivate(context));
    }

}
