package clustercode.main.modules;

import clustercode.api.config.ConfigLoader;
import clustercode.api.domain.Activator;
import clustercode.api.transcode.Transcoder;
import clustercode.api.transcode.TranscodingService;
import clustercode.impl.transcode.TranscodeActivator;
import clustercode.impl.transcode.TranscoderConfig;
import clustercode.impl.transcode.TranscodingServiceImpl;
import clustercode.impl.util.InvalidConfigurationException;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import lombok.var;

import java.util.HashMap;
import java.util.Map;

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

        try {
            var map = getTranscoderMap();
            install(map.get(config.transcoder_type()));
        } catch (UnsupportedOperationException ex) {
            addError(new InvalidConfigurationException(
                    "You did not configure the transcoder type correctly. Consult the docs!", ex));
        }

    }

    private Map<Transcoder, Module> getTranscoderMap() {
        Map<Transcoder, Module> map = new HashMap<>();
        map.put(Transcoder.FFMPEG, new FfmpegModule());
        map.put(Transcoder.HANDBRAKE, new HandbrakeModule());
        return map;
    }
}
