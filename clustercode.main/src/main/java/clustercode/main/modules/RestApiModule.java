package clustercode.main.modules;

import clustercode.api.config.ConfigLoader;
import clustercode.api.domain.Activator;
import clustercode.api.rest.v1.ProgressReportAdapter;
import clustercode.api.rest.v1.RestServiceConfig;
import clustercode.api.rest.v1.RestServicesActivator;
import clustercode.api.rest.v1.hook.ProgressHook;
import clustercode.api.rest.v1.hook.ProgressHookImpl;
import clustercode.api.rest.v1.hook.TaskHook;
import clustercode.api.rest.v1.hook.TaskHookImpl;
import clustercode.api.rest.v1.impl.FfmpegProgressAdapter;
import clustercode.api.rest.v1.impl.HandbrakeProgressAdapter;
import clustercode.api.transcode.TranscodeReport;
import clustercode.api.transcode.Transcoder;
import clustercode.api.transcode.output.FfmpegOutput;
import clustercode.api.transcode.output.HandbrakeOutput;
import clustercode.impl.util.InvalidConfigurationException;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
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
            var adapterMap = getAdapterMap();
            bind(new TypeLiteral<ProgressReportAdapter>() {
            }).to(adapterMap.get(config.transcoder_type()));

            var progressMap = getProgressMap();
            MapBinder<Transcoder, TranscodeReport> progressMapBinder = MapBinder.newMapBinder(binder(),
                    Transcoder.class, TranscodeReport.class);
            progressMapBinder.addBinding(config.transcoder_type()).to(progressMap.get(config.transcoder_type()));
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
        Multibinder<Activator> multibinder = Multibinder.newSetBinder(binder(), Activator.class);
        multibinder.addBinding().to(RestServicesActivator.class).in(Singleton.class);
    }

    private Map<Transcoder, Class<? extends ProgressReportAdapter>> getAdapterMap() {
        Map<Transcoder, Class<? extends ProgressReportAdapter>> map = new HashMap<>();
        map.put(Transcoder.FFMPEG, FfmpegProgressAdapter.class);
        map.put(Transcoder.HANDBRAKE, HandbrakeProgressAdapter.class);
        return map;
    }

    private Map<Transcoder, Class<? extends TranscodeReport>> getProgressMap() {
        Map<Transcoder, Class<? extends TranscodeReport>> map = new HashMap<>();
        map.put(Transcoder.FFMPEG, FfmpegOutput.class);
        map.put(Transcoder.HANDBRAKE, HandbrakeOutput.class);
        return map;
    }
}
