package clustercode.main.modules;

import clustercode.api.cluster.ClusterService;
import clustercode.api.config.ConfigLoader;
import clustercode.api.domain.Activator;
import clustercode.impl.cluster.jgroups.JGroupsClusterFacade;
import clustercode.impl.cluster.jgroups.JgroupsClusterActivator;
import clustercode.impl.cluster.jgroups.SingleNodeClusterImpl;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

class JGroupsModule extends ConfigurableModule {

    JGroupsModule(ConfigLoader loader) {
        super(loader);
    }

    @Override
    protected void configure() {

        bind(ClusterService.class).to(JGroupsClusterFacade.class).in(Singleton.class);
        bind(SingleNodeClusterImpl.class).in(Singleton.class);

        Multibinder<Activator> multibinder = Multibinder.newSetBinder(binder(), Activator.class);
        multibinder.addBinding().to(JgroupsClusterActivator.class).in(Singleton.class);
    }

}
