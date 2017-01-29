package net.chrigel.clustercode.transcode.impl;

import net.chrigel.clustercode.transcode.Transcoder;
import net.chrigel.clustercode.util.di.AbstractPropertiesModule;
import net.chrigel.clustercode.util.ConfigurationHelper;

import java.io.IOException;
import java.util.Properties;

public class TranscodeModule extends AbstractPropertiesModule {


    public static final String TRANSCODE_CLI_KEY = "CC_TRANSCODE_CLI";
    private final String propertiesFilename;

    public TranscodeModule(String propertiesFilename) {
        this.propertiesFilename = propertiesFilename;
    }

    @Override
    protected void configure() {

        try {
            Properties properties = ConfigurationHelper.loadPropertiesFromFile(propertiesFilename);
            String transcoder = properties.getProperty(TRANSCODE_CLI_KEY, "FFMPEG");
            bind(Transcoder.class).to(TranscoderImpl.class);
        } catch (IOException | IllegalArgumentException e) {
            addError(e);
        }

    }

}
