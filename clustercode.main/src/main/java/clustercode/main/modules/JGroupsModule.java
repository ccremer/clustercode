package clustercode.main.modules;

import clustercode.api.cluster.ClusterService;
import clustercode.api.cluster.JGroupsMessageDispatcher;
import clustercode.api.cluster.JGroupsTaskState;
import clustercode.api.config.ConfigLoader;
import clustercode.api.domain.Activator;
import clustercode.impl.cluster.jgroups.*;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

class JGroupsModule extends ConfigurableModule {

    JGroupsModule(ConfigLoader loader) {
        super(loader);
    }

    @Override
    protected void configure() {
        bind(JgroupsClusterConfig.class).toInstance(
                loader.getConfig(JgroupsClusterConfig.class));

        bind(ClusterService.class).to(JGroupsClusterFacade.class).in(Singleton.class);
        bind(SingleNodeClusterImpl.class).in(Singleton.class);
        bind(JGroupsMessageDispatcher.class).to(JGroupsMessageDispatcherImpl.class);
        bind(JGroupsTaskState.class).to(JGroupsTaskStateImpl.class);

        Multibinder<Activator> multibinder = Multibinder.newSetBinder(binder(), Activator.class);
        multibinder.addBinding().to(JgroupsClusterActivator.class).in(Singleton.class);
    }

}
