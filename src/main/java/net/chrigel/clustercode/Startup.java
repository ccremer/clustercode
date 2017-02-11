package net.chrigel.clustercode;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.cleanup.impl.CleanupModule;
import net.chrigel.clustercode.cluster.impl.ClusterModule;
import net.chrigel.clustercode.constraint.impl.ConstraintModule;
import net.chrigel.clustercode.process.impl.ProcessModule;
import net.chrigel.clustercode.scan.impl.ScanModule;
import net.chrigel.clustercode.transcode.impl.TranscodeModule;
import net.chrigel.clustercode.util.ConfigurationHelper;
import net.chrigel.clustercode.util.InvalidConfigurationException;
import net.chrigel.clustercode.util.di.PropertiesModule;
import net.chrigel.clustercode.statemachine.StateController;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 *
 */
@XSlf4j
public class Startup {

    public static void main(String[] args) throws Exception {

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            // Normally the exceptions will be caught, but to debug any unexpected ones we log them (again).
            log.error("Application-wide uncaught exception:", throwable);
            System.exit(1);
        });

        log.info("Working dir: {}", new File("").getAbsolutePath());

        if (args == null || args.length == 0) {
            log.error("Configuration Error: ", new InvalidConfigurationException("Configuration file not provided in " +
                    "arguments."));
            System.exit(2);
        }
        String configFileName = args[0];

        log.info("Reading configuration file {}...", configFileName);
        Properties config = ConfigurationHelper.loadPropertiesFromFile(configFileName);

        log.debug("Creating guice modules...");
        List<Module> modules = new LinkedList<>();

        modules.add(new CleanupModule(config));
        modules.add(new ClusterModule());
        modules.add(new ConstraintModule(config));
        modules.add(new ProcessModule());
        modules.add(new ScanModule(config));
        modules.add(new TranscodeModule());
        modules.add(new PropertiesModule(config));

        log.info("Booting application...");
        Injector injector = Guice.createInjector(modules);

        injector.getInstance(StateController.class).initialize();
    }

}
