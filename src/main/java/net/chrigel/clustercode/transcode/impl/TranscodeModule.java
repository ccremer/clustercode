package net.chrigel.clustercode.transcode.impl;

import net.chrigel.clustercode.transcode.TranscoderSettings;
import net.chrigel.clustercode.transcode.TranscodingService;
import net.chrigel.clustercode.util.di.AbstractPropertiesModule;

public class TranscodeModule extends AbstractPropertiesModule {


    public static final String TRANSCODE_CLI_KEY = "CC_TRANSCODE_CLI";
    public static final String TRANSCODE_TEMPDIR_KEY = "CC_TRANSCODE_TEMP_DIR";
    public static final String TRANSCODE_IO_REDIRECTED_KEY = "CC_TRANSCODE_IO_REDIRECTED";
    public static final String TRANSCODE_DEFAULT_FORMAT_KEY = "CC_TRANSCODE_DEFAULT_FORMAT";

    @Override
    protected void configure() {

        bind(TranscodingService.class).to(TranscodingServiceImpl.class);
        bind(TranscoderSettings.class).to(TranscoderSettingsImpl.class);
    }

}
