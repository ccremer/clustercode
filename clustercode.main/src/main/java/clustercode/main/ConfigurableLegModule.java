package clustercode.main;

import clustercode.api.config.ConfigLoader;
import com.google.inject.Module;

public interface ConfigurableLegModule extends Module {

    ConfigurableLegModule setConfig(ConfigLoader loader);

}
