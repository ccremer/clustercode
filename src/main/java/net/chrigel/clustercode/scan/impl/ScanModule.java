package net.chrigel.clustercode.scan.impl;

import com.google.inject.multibindings.Multibinder;
import net.chrigel.clustercode.scan.*;
import net.chrigel.clustercode.scan.impl.matcher.Matchers;
import net.chrigel.clustercode.scan.impl.matcher.ConfigurableMatcherStrategy;
import net.chrigel.clustercode.util.di.AbstractPropertiesModule;

import java.util.Arrays;
import java.util.Locale;

/**
 * Provides a guice module for configuring the scanning features.
 */
public class ScanModule extends AbstractPropertiesModule {

    public static final String PROFILE_FILE_NAME_KEY = "CC_PROFILE_FILE_NAME";
    public static final String PROFILE_FILE_EXTENSION_KEY = "CC_PROFILE_FILE_EXTENSION";
    public static final String PROFILE_FILE_DEFAULT_KEY = "CC_PROFILE_FILE_DEFAULT";
    public static final String PROFILE_DIRECTORY_KEY = "CC_PROFILE_DIR";
    public static final String PROFILE_STRATEGY_KEY = "CC_PROFILE_STRATEGY";
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
        bind(ProfileScanSettings.class).to(ProfileScanSettingsImpl.class);

        loadPropertiesFromFile("").ifPresent(properties -> {
            String strategies = getProperty(properties, PROFILE_STRATEGY_KEY);
            String[] arr = strategies.trim().split(" ");
            Multibinder<ProfileMatcher> matcherBinder = Multibinder.newSetBinder(binder(), ProfileMatcher.class);
            Arrays.asList(arr).forEach(strategy -> {
                try {
                    matcherBinder.addBinding().to(Matchers.valueOf(
                            strategy.toUpperCase(Locale.ENGLISH)).getImplementingClass());
                } catch (IllegalArgumentException ex) {
                    addError(ex);
                }
            });
        });

        bind(ProfileMatcherStrategy.class).to(ConfigurableMatcherStrategy.class);
    }

}
