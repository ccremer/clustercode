package clustercode.main.modules;

import clustercode.api.config.ConfigLoader;
import clustercode.api.domain.Activator;
import clustercode.api.scan.*;
import clustercode.impl.scan.*;
import clustercode.impl.scan.matcher.*;
import clustercode.impl.util.InvalidConfigurationException;
import clustercode.impl.util.di.ModuleHelper;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides a guice module for configuring the scanning features.
 */
public class ScanModule extends ConfigurableModule {

    public ScanModule(ConfigLoader loader) {
        super(loader);
    }

    @Override
    protected void configure() {
        var mediaScanConfig = loader.getConfig(MediaScanConfig.class);
        checkInterval(mediaScanConfig.media_scan_interval());

        ProfileScanConfig profileScanConfig = loader.getConfig(ProfileScanConfig.class);
        bind(ProfileScanConfig.class).toInstance(profileScanConfig);

        bind(MediaScanConfig.class).toInstance(mediaScanConfig);

        bind(FileScanner.class).to(FileScannerImpl.class);
        bind(MediaScanService.class).to(MediaScanServiceImpl.class);

        bind(SelectionService.class).to(SelectionServiceImpl.class);

        bind(ProfileScanService.class).to(ProfileScanServiceImpl.class);
        bind(ProfileParser.class).to(ProfileParserImpl.class);

        ModuleHelper.verifyIn(profileScanConfig.profile_matchers())
                    .that(ProfileMatchers.COMPANION)
                    .isBefore(ProfileMatchers.DEFAULT);

        ModuleHelper.verifyIn(profileScanConfig.profile_matchers())
                    .that(ProfileMatchers.DIRECTORY_STRUCTURE)
                    .isBefore(ProfileMatchers.DEFAULT);

        var mapBinder = MapBinder.newMapBinder(binder(), ProfileMatchers.class, ProfileMatcher.class);
        var map = getMatcherMap();
        profileScanConfig.profile_matchers().forEach(matcher -> mapBinder.addBinding(matcher).to(map.get(matcher)));

        var multibinder = Multibinder.newSetBinder(binder(), Activator.class);
        multibinder.addBinding().to(ScanServicesActivator.class).in(Singleton.class);
    }

    private void checkInterval(long scanInterval) {
        if (scanInterval < 1) {
            throw new InvalidConfigurationException("The scan interval must be >= 1.");
        }
    }

    private Map<ProfileMatchers, Class<? extends ProfileMatcher>> getMatcherMap() {
        Map<ProfileMatchers, Class<? extends ProfileMatcher>> map = new HashMap<>();
        map.put(ProfileMatchers.COMPANION, CompanionProfileMatcher.class);
        map.put(ProfileMatchers.DEFAULT, DefaultProfileMatcher.class);
        map.put(ProfileMatchers.DIRECTORY_STRUCTURE, DirectoryStructureMatcher.class);
        return map;
    }

}
