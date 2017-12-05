package net.chrigel.clustercode.transcode.impl;

import com.google.inject.TypeLiteral;
import net.chrigel.clustercode.event.EventBus;
import net.chrigel.clustercode.event.impl.EventBusImpl;
import net.chrigel.clustercode.process.OutputParser;
import net.chrigel.clustercode.transcode.TranscodeConnector;
import net.chrigel.clustercode.transcode.TranscoderSettings;
import net.chrigel.clustercode.transcode.TranscodingService;
import net.chrigel.clustercode.transcode.impl.ffmpeg.FfmpegOutput;
import net.chrigel.clustercode.transcode.impl.ffmpeg.FfmpegParser;
import net.chrigel.clustercode.transcode.impl.ffmpeg.FfprobeOutput;
import net.chrigel.clustercode.transcode.impl.ffmpeg.FfprobeParser;
import net.chrigel.clustercode.transcode.impl.handbrake.HandbrakeOutput;
import net.chrigel.clustercode.transcode.impl.handbrake.HandbrakeParser;
import net.chrigel.clustercode.transcode.messages.TranscodeMessage;
import net.chrigel.clustercode.util.di.AbstractPropertiesModule;

import javax.inject.Singleton;
import java.util.Locale;
import java.util.Properties;

public class TranscodeModule extends AbstractPropertiesModule {


    public static final String TRANSCODE_CLI_KEY = "CC_TRANSCODE_CLI";
    public static final String TRANSCODE_TYPE_KEY = "CC_TRANSCODE_TYPE";
    public static final String TRANSCODE_TEMPDIR_KEY = "CC_TRANSCODE_TEMP_DIR";
    public static final String TRANSCODE_IO_REDIRECTED_KEY = "CC_TRANSCODE_IO_REDIRECTED";
    public static final String TRANSCODE_DEFAULT_FORMAT_KEY = "CC_TRANSCODE_DEFAULT_FORMAT";
    private final Properties properties;

    public TranscodeModule(Properties properties) {
        this.properties = properties;
    }

    @Override
    protected void configure() {

        bind(TranscodingService.class).to(TranscodingServiceImpl.class).in(Singleton.class);
        bind(TranscoderSettings.class).to(TranscoderSettingsImpl.class);
        bind(TranscodeConnector.class).to(TranscodeConnectorImpl.class).asEagerSingleton();

        String type = getEnvironmentVariableOrProperty(properties, TRANSCODE_TYPE_KEY).toUpperCase(Locale.ENGLISH);
        bind(ProgressCalculator.class).to(Transcoder.valueOf(type).getImplementingClass());

        bind(new TypeLiteral<EventBus<TranscodeMessage>>(){}).toInstance(new EventBusImpl<>());

        installFfmpeg();
        installHandbrake();
    }

    private void installHandbrake() {

        bind(new TypeLiteral<OutputParser<HandbrakeOutput>>() {
        }).to(HandbrakeParser.class);
    }

    private void installFfmpeg() {

        bind(new TypeLiteral<OutputParser<FfmpegOutput>>() {
        }).to(FfmpegParser.class);

        bind(new TypeLiteral<OutputParser<FfprobeOutput>>() {
        }).to(FfprobeParser.class);
    }

}
