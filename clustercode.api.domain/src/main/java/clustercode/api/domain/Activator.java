package clustercode.api.domain;

public interface Activator {

    abstract void activate(ActivatorContext context);

    void deactivate(ActivatorContext context);

}
