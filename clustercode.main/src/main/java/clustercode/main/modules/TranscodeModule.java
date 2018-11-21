package clustercode.main.modules;

import clustercode.api.config.ConfigLoader;
import clustercode.api.domain.Activator;
import clustercode.api.transcode.TranscodingService;
import clustercode.impl.transcode.TranscodeActivator;
import clustercode.impl.transcode.TranscoderConfig;
import clustercode.impl.transcode.TranscodingServiceImpl;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

public class TranscodeModule extends ConfigurableModule {

    public TranscodeModule(ConfigLoader loader) {
        super(loader);
    }

    @Override
    protected void configure() {
        var config = loader.getConfig(TranscoderConfig.class);
        bind(TranscoderConfig.class).toInstance(config);

        bind(TranscodingService.class).to(TranscodingServiceImpl.class).in(Singleton.class);
        Multibinder<Activator> multibinder = Multibinder.newSetBinder(binder(), Activator.class);
        multibinder.addBinding().to(TranscodeActivator.class).in(Singleton.class);

    }

}
