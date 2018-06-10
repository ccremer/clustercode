package net.chrigel.clustercode.scan.impl;

import com.google.inject.multibindings.Multibinder;
import net.chrigel.clustercode.scan.*;
import net.chrigel.clustercode.scan.matcher.ConfigurableMatcherStrategy;
import net.chrigel.clustercode.scan.matcher.ProfileMatchers;
import net.chrigel.clustercode.util.di.AbstractPropertiesModule;
import net.chrigel.clustercode.util.di.ModuleHelper;

import java.util.Properties;

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
    public static final String MEDIA_SCAN_INTERVAL_KEY = "CC_MEDIA_SCAN_INTERVAL";

    private final Properties properties;

    public ScanModule(Properties properties) {
        this.properties = properties;
    }

    @Override
    protected void configure() {
        bind(FileScanner.class).to(FileScannerImpl.class);
        bind(MediaScanSettings.class).to(MediaScanSettingsImpl.class);
        bind(MediaScanService.class).to(MediaScanServiceImpl.class);

        bind(SelectionService.class).to(SelectionServiceImpl.class);

        bind(ProfileScanService.class).to(ProfileScanServiceImpl.class);
        bind(ProfileParser.class).to(ProfileParserImpl.class);
        bind(ProfileScanSettings.class).to(ProfileScanSettingsImpl.class);

        String strategies = getEnvironmentVariableOrProperty(properties, PROFILE_STRATEGY_KEY);

        ModuleHelper.checkStrategiesForOrder(strategies, PROFILE_STRATEGY_KEY,
                ProfileMatchers.COMPANION.name(), ProfileMatchers.DEFAULT.name());
        ModuleHelper.checkStrategiesForOrder(strategies, PROFILE_STRATEGY_KEY,
                ProfileMatchers.DIRECTORY_STRUCTURE.name(), ProfileMatchers.DEFAULT.name());

        Multibinder<ProfileMatcher> matcherBinder = Multibinder.newSetBinder(binder(), ProfileMatcher.class);
        try {
            ModuleHelper.bindStrategies(matcherBinder, strategies, ProfileMatchers::valueOf);
        } catch (IllegalArgumentException ex) {
            addError(ex);
        }

        bind(ProfileMatcherStrategy.class).to(ConfigurableMatcherStrategy.class);
    }

}
