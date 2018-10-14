package clustercode.main;

import clustercode.api.config.ConfigLoader;
import clustercode.api.event.RxEventBus;
import clustercode.api.event.messages.StartupCompletedEvent;
import clustercode.main.modules.*;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import lombok.extern.slf4j.XSlf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.jar.Manifest;

@XSlf4j
public class GuiceManager {

    private Injector injector;
    private final List<Module> modules;

    public GuiceManager(ConfigLoader loader) {
        log.debug("Creating guice modules...");
        modules = new LinkedList<>();

        modules.add(new GlobalModule());
        modules.add(new CleanupModule(loader));
        modules.add(new ClusterModule(loader));
        modules.add(new ConstraintModule(loader));
        modules.add(new ProcessModule());
        modules.add(new ScanModule(loader));
        modules.add(new TranscodeModule(loader));
        modules.add(new RestApiModule(loader));

    }

    void start() {
        log.info("Booting clustercode {}...", getApplicationVersion().orElse("unknown"));
        injector = Guice.createInjector(modules);
        ComponentActivator componentActivator = injector.getInstance(ComponentActivator.class);
        log.info("Preparing components...");
        componentActivator.preActivateServices();
        log.info("Activating components...");
        componentActivator.activateServices();
        log.info("Bootup complete.");
        injector.getInstance(RxEventBus.class)
                .emit(StartupCompletedEvent
                        .builder()
                        .mainVersion(getApplicationVersion().orElse("unknown"))
                        .build());
    }

    public <T> T getInstance(Class<T> clazz) {
        return injector.getInstance(clazz);
    }

    public static Optional<String> getApplicationVersion() {
        InputStream stream = ClassLoader.getSystemResourceAsStream("META-INF/MANIFEST.MF");
        try {
            return Optional.ofNullable(
                    new Manifest(stream)
                            .getMainAttributes()
                            .getValue("Implementation-VersionInfo"));
        } catch (IOException | NullPointerException e) {
            log.catching(e);
        }
        return Optional.empty();
    }


}
