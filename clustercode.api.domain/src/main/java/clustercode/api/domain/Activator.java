package clustercode.api.domain;

public interface Activator {

    void preActivate(ActivatorContext context);

    void activate(ActivatorContext context);

    void deactivate(ActivatorContext context);

}
