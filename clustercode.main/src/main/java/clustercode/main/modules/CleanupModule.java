package clustercode.main.modules;

import clustercode.api.cleanup.CleanupProcessor;
import clustercode.api.cleanup.CleanupService;
import clustercode.api.config.ConfigLoader;
import clustercode.api.domain.Activator;
import clustercode.impl.cleanup.CleanupActivator;
import clustercode.impl.cleanup.CleanupConfig;
import clustercode.impl.cleanup.CleanupServiceImpl;
import clustercode.impl.cleanup.processor.*;
import clustercode.impl.util.di.ModuleHelper;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;

import java.util.HashMap;
import java.util.Map;

public class CleanupModule extends ConfigurableModule {

    public CleanupModule(ConfigLoader loader) {
        super(loader);
    }

    @Override
    protected void configure() {
        CleanupConfig config = loader.getConfig(CleanupConfig.class);
        bind(CleanupConfig.class).toInstance(config);

        bind(CleanupService.class).to(CleanupServiceImpl.class);

        try {
            ModuleHelper.verifyIn(config.cleanup_processors())
                        .that(CleanupProcessors.STRUCTURED_OUTPUT)
                        .isNotGivenTogetherWith(CleanupProcessors.UNIFIED_OUTPUT);

            ModuleHelper.verifyIn(config.cleanup_processors())
                        .that(CleanupProcessors.DELETE_SOURCE)
                        .isNotGivenTogetherWith(CleanupProcessors.MARK_SOURCE);
        } catch (IllegalArgumentException ex) {
            addError(ex);
        }

        MapBinder<CleanupProcessors, CleanupProcessor> mapBinder = MapBinder.newMapBinder(binder(), CleanupProcessors
                .class, CleanupProcessor.class);

        Map<CleanupProcessors, Class<? extends CleanupProcessor>> assignments = createClassAssignments();
        config.cleanup_processors().forEach(entry -> mapBinder.addBinding(entry).to(assignments.get(entry)));

        Multibinder<Activator> multibinder = Multibinder.newSetBinder(binder(), Activator.class);
        multibinder.addBinding().to(CleanupActivator.class).in(Singleton.class);
    }

    private Map<CleanupProcessors, Class<? extends CleanupProcessor>> createClassAssignments() {
        Map<CleanupProcessors, Class<? extends CleanupProcessor>> map = new HashMap<>();
        map.put(CleanupProcessors.CHOWN, ChangeOwnerProcessor.class);
        map.put(CleanupProcessors.DELETE_SOURCE, DeleteSourceProcessor.class);
        map.put(CleanupProcessors.MARK_SOURCE, MarkSourceProcessor.class);
        map.put(CleanupProcessors.MARK_SOURCE_DIR, MarkSourceDirProcessor.class);
        map.put(CleanupProcessors.STRUCTURED_OUTPUT, StructuredOutputDirectoryProcessor.class);
        map.put(CleanupProcessors.UNIFIED_OUTPUT, UnifiedOutputDirectoryProcessor.class);
        return map;
    }
}
