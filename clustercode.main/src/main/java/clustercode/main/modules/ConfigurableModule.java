package clustercode.main.modules;

import clustercode.api.config.ConfigLoader;
import com.google.inject.AbstractModule;

public abstract class ConfigurableModule extends AbstractModule {

    protected final ConfigLoader loader;

    public ConfigurableModule(ConfigLoader loader) {
        this.loader = loader;
    }

}
