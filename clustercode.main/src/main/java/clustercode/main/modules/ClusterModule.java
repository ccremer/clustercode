package clustercode.main.modules;

import clustercode.api.config.ConfigLoader;
import clustercode.impl.util.InvalidConfigurationException;
import clustercode.main.config.ClusterConfig;

public class ClusterModule extends ConfigurableModule {

    public ClusterModule(ConfigLoader loader) {
        super(loader);
    }

    @Override
    protected void configure() {
        ClusterConfig base_config = loader.getConfig(ClusterConfig.class);

        try {
            // I don't like switches, but here we don't need over engineering.
            switch (base_config.cluster_type()) {
                case JGROUPS:
                    install(new JGroupsModule(loader));
                    break;
                default:
                    throw new EnumConstantNotPresentException(ClusterType.class, base_config.cluster_type().name());
            }
        } catch (UnsupportedOperationException ex) {
            throw new InvalidConfigurationException("You configured the CC_CLUSTER_TYPE incorrectly. Consult the docs!");
        }
    }
}
