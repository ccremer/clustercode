package clustercode.main.modules;

import clustercode.api.config.ConfigLoader;
import clustercode.api.domain.Activator;
import clustercode.api.rest.v1.RestServiceConfig;
import clustercode.api.rest.v1.RestServicesActivator;
import clustercode.api.rest.v1.hook.ProgressHook;
import clustercode.api.rest.v1.hook.ProgressHookImpl;
import clustercode.api.rest.v1.hook.TaskHook;
import clustercode.api.rest.v1.hook.TaskHookImpl;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.owlike.genson.ext.jaxrs.GensonJsonConverter;
import io.logz.guice.jersey.JerseyModule;
import io.logz.guice.jersey.configuration.JerseyConfiguration;

public class RestApiModule extends ConfigurableModule {

    public RestApiModule(ConfigLoader loader) {
        super(loader);
    }

    @Override
    protected void configure() {
        RestServiceConfig config = loader.getConfig(RestServiceConfig.class);
        bind(RestServiceConfig.class).toInstance(config);

        if (config.rest_enabled()) {
            installJersey(config.rest_api_port());
        }

        bind(ProgressHook.class).to(ProgressHookImpl.class).in(Singleton.class);
        bind(TaskHook.class).to(TaskHookImpl.class).in(Singleton.class);
    }

    private void installJersey(int port) {

        JerseyConfiguration configuration = JerseyConfiguration
                .builder()
                .addPackage("clustercode.api.rest")
                .addPort(port)
                .withContextPath("/api")
                .registerClasses(GensonJsonConverter.class)
                .build();

        install(new JerseyModule(configuration));
        Multibinder<Activator> multibinder = Multibinder.newSetBinder(binder(), Activator.class);
        multibinder.addBinding().to(RestServicesActivator.class).in(Singleton.class);
    }

}
