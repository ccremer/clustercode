package clustercode.main.modules;

import clustercode.api.config.ConfigLoader;
import clustercode.api.transcode.Transcoder;
import clustercode.impl.transcode.TranscoderConfig;
import clustercode.api.transcode.TranscodingService;
import clustercode.impl.transcode.TranscodeActivator;
import clustercode.impl.transcode.TranscodingServiceImpl;
import clustercode.impl.util.InvalidConfigurationException;
import com.google.inject.Module;
import lombok.var;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

public class TranscodeModule extends ConfigurableModule {

    public TranscodeModule(ConfigLoader loader) {
        super(loader);
    }

    @Override
    protected void configure() {
        TranscoderConfig config = loader.getConfig(TranscoderConfig.class);
        bind(TranscoderConfig.class).toInstance(config);

        bind(TranscodingService.class).to(TranscodingServiceImpl.class).in(Singleton.class);
        bind(TranscodeActivator.class).asEagerSingleton();

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
