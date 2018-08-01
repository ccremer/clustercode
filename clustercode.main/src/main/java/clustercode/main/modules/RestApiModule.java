package clustercode.main.modules;

import clustercode.api.config.ConfigLoader;
import clustercode.api.rest.v1.ProgressReportAdapter;
import clustercode.api.rest.v1.RestServiceConfig;
import clustercode.api.rest.v1.RestServicesActivator;
import clustercode.api.rest.v1.hook.ProgressHook;
import clustercode.api.rest.v1.hook.ProgressHookImpl;
import clustercode.api.rest.v1.hook.TaskHook;
import clustercode.api.rest.v1.hook.TaskHookImpl;
import clustercode.api.rest.v1.impl.FfmpegProgressAdapter;
import clustercode.api.rest.v1.impl.HandbrakeProgressAdapter;
import clustercode.api.transcode.Transcoder;
import clustercode.impl.util.InvalidConfigurationException;
import com.google.inject.TypeLiteral;
import com.owlike.genson.ext.jaxrs.GensonJsonConverter;
import io.logz.guice.jersey.JerseyModule;
import io.logz.guice.jersey.configuration.JerseyConfiguration;
import lombok.var;

import java.util.HashMap;
import java.util.Map;

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

        try {
            var map = getAdapterMap();
            bind(new TypeLiteral<ProgressReportAdapter>() {
            }).to(map.get(config.transcoder_type()));
        } catch (UnsupportedOperationException ex) {
            throw new InvalidConfigurationException(
                    "You configured the CC_TRANSCODE_TYPE incorrectly. Consult the docs!");
        }

        bind(ProgressHook.class).to(ProgressHookImpl.class).asEagerSingleton();
        bind(TaskHook.class).to(TaskHookImpl.class).asEagerSingleton();
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
        bind(RestServicesActivator.class).asEagerSingleton();
    }

    private Map<Transcoder, Class<? extends ProgressReportAdapter>> getAdapterMap() {
        Map<Transcoder, Class<? extends ProgressReportAdapter>> map = new HashMap<>();
        map.put(Transcoder.FFMPEG, FfmpegProgressAdapter.class);
        map.put(Transcoder.HANDBRAKE, HandbrakeProgressAdapter.class);
        return map;
    }
}
