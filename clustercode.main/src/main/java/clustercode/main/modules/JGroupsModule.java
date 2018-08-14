package clustercode.main.modules;

import clustercode.api.cluster.ClusterService;
import clustercode.api.cluster.JGroupsMessageDispatcher;
import clustercode.api.cluster.JGroupsTaskState;
import clustercode.api.config.ConfigLoader;
import clustercode.impl.cluster.jgroups.*;
import com.google.inject.Singleton;

class JGroupsModule extends ConfigurableModule {

    JGroupsModule(ConfigLoader loader) {
        super(loader);
    }

    @Override
    protected void configure() {
        bind(JgroupsClusterConfig.class).toInstance(
                loader.getConfig(JgroupsClusterConfig.class));

        bind(ClusterService.class).to(JgroupsClusterImpl.class);
        bind(JgroupsClusterImpl.class).in(Singleton.class);
        bind(JGroupsMessageDispatcher.class).to(JGroupsMessageDispatcherImpl.class);
        bind(JGroupsTaskState.class).to(JGroupsTaskStateImpl.class);

        bind(JgroupsClusterActivator.class).asEagerSingleton();
    }

}
