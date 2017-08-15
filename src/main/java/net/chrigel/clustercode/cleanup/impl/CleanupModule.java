package net.chrigel.clustercode.cleanup.impl;

import com.google.inject.multibindings.Multibinder;
import net.chrigel.clustercode.cleanup.CleanupProcessor;
import net.chrigel.clustercode.cleanup.CleanupService;
import net.chrigel.clustercode.cleanup.CleanupSettings;
import net.chrigel.clustercode.cleanup.CleanupStrategy;
import net.chrigel.clustercode.cleanup.processor.CleanupProcessors;
import net.chrigel.clustercode.cleanup.processor.ConfigurableCleanupStrategy;
import net.chrigel.clustercode.util.InvalidConfigurationException;
import net.chrigel.clustercode.util.di.AbstractPropertiesModule;
import net.chrigel.clustercode.util.di.ModuleHelper;

import java.util.Locale;
import java.util.Properties;

public class CleanupModule extends AbstractPropertiesModule {

    public static final String CLEANUP_STRATEGY_KEY = "CC_CLEANUP_STRATEGY";
    public static final String CLEANUP_OUTPUT_DIR_KEY = "CC_MEDIA_OUTPUT_DIR";
    public static final String CLEANUP_OUTPUT_OVERWRITE_KEY = "CC_CLEANUP_OVERWRITE";

    private final Properties properties;

    public CleanupModule(Properties properties) {
        this.properties = properties;
    }

    @Override
    protected void configure() {

        bind(CleanupService.class).to(CleanupServiceImpl.class);
        bind(CleanupStrategy.class).to(ConfigurableCleanupStrategy.class);
        bind(CleanupSettings.class).to(CleanupSettingsImpl.class);

        String strategies = getEnvironmentVariableOrProperty(properties, CLEANUP_STRATEGY_KEY).toUpperCase(Locale.ENGLISH);

        Multibinder<CleanupProcessor> cleanupBinder = Multibinder.newSetBinder(binder(), CleanupProcessor.class);
        try {
            ModuleHelper.bindStrategies(cleanupBinder, strategies, CleanupProcessors::valueOf);
        } catch (IllegalArgumentException ex) {
            addError(new InvalidConfigurationException("Invalid cleanup strategy: {}.", ex.getMessage()));
        }

        ModuleHelper.checkStrategiesForIncompatibilities(strategies, CLEANUP_STRATEGY_KEY,
                CleanupProcessors.STRUCTURED_OUTPUT.name(), CleanupProcessors.UNIFIED_OUTPUT.name());
        ModuleHelper.checkStrategiesForIncompatibilities(strategies, CLEANUP_STRATEGY_KEY,
                CleanupProcessors.DELETE_SOURCE.name(), CleanupProcessors.MARK_SOURCE.name());

    }


}
