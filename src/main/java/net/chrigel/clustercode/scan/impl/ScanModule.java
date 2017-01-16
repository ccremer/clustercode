package net.chrigel.clustercode.scan.impl;

import com.google.inject.AbstractModule;
import net.chrigel.clustercode.scan.*;
import net.chrigel.clustercode.scan.impl.matcher.MostAppropriateMatcherStrategy;

/**
 * Provides a guice module for configuring the scanning features.
 */
public class ScanModule extends AbstractModule {

    public static final String PROFILE_FILE_NAME_KEY = "CC_PROFILE_FILE_NAME";
    public static final String MEDIA_INPUT_DIR_KEY = "CC_MEDIA_INPUT_DIR";
    public static final String MEDIA_EXTENSIONS_KEY = "CC_MEDIA_EXTENSIONS";
    public static final String MEDIA_SKIP_NAME_KEY = "CC_MEDIA_SKIP_NAME";

    @Override
    protected void configure() {
        bind(FileScanner.class).to(FileScannerImpl.class);
        bind(MediaScanSettings.class).to(MediaScanSettingsImpl.class);
        bind(MediaScanService.class).to(MediaScanServiceImpl.class);

        bind(ProfileScanService.class).to(ProfileScanServiceImpl.class);
        bind(ProfileParser.class).to(ProfileParserImpl.class);

        bind(ProfileMatcherStrategy.class).to(MostAppropriateMatcherStrategy.class);
    }

}
