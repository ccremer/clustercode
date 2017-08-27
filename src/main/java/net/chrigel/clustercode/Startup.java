package net.chrigel.clustercode;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import net.chrigel.clustercode.cleanup.impl.CleanupModule;
import net.chrigel.clustercode.cluster.impl.ClusterModule;
import net.chrigel.clustercode.constraint.impl.ConstraintModule;
import net.chrigel.clustercode.api.RestApiServices;
import net.chrigel.clustercode.api.impl.ApiModule;
import net.chrigel.clustercode.process.impl.ProcessModule;
import net.chrigel.clustercode.scan.impl.ScanModule;
import net.chrigel.clustercode.statemachine.StateMachineService;
import net.chrigel.clustercode.statemachine.actions.ActionModule;
import net.chrigel.clustercode.statemachine.states.StateMachineModule;
import net.chrigel.clustercode.transcode.impl.TranscodeModule;
import net.chrigel.clustercode.util.ConfigurationHelper;
import net.chrigel.clustercode.util.di.EnvironmentModule;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.jar.Manifest;

/**
 * Provides the entry point of the application. The main method expects the path to the configuration file as an
 * environment variable.
 */
public class Startup {

    private static XLogger log;

    public static void main(String[] args) throws Exception {

        List<String> logFiles = Arrays.asList("log4j2.xml", System.getenv("CC_LOG_CONFIG_FILE"));

        logFiles.forEach(name -> {
            if (name == null) return;
            Path logFile = Paths.get(name);
            if (Files.exists(logFile)) {
                System.setProperty("log4j.configurationFile", logFile.toAbsolutePath().toString());
            }
        });
        log = XLoggerFactory.getXLogger(Startup.class);

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            // Normally the exceptions will be caught, but to debug any unexpected ones we log them (again).
            log.error("Application-wide uncaught exception:", throwable);
            System.exit(1);
        });

        log.info("Working dir: {}", new File("").getAbsolutePath());

        Properties config = getProperties(args.length >= 1 ? args[0] : null);

        log.debug("Creating guice modules...");
        List<Module> modules = new LinkedList<>();

        modules.add(new CleanupModule(config));
        modules.add(new ClusterModule());
        modules.add(new ConstraintModule(config));
        modules.add(new ProcessModule());
        modules.add(new ScanModule(config));
        modules.add(new TranscodeModule(config));
        modules.add(new StateMachineModule());
        modules.add(new ActionModule());
        modules.add(new EnvironmentModule(config));
        modules.add(new ApiModule(config));

        log.info("Booting clustercode {}...", getApplicationVersion());
        Injector injector = Guice.createInjector(modules);

        injector.getInstance(StateMachineService.class).initialize();
        injector.getInstance(RestApiServices.class).start();
    }

    private static String getApplicationVersion() {
        InputStream stream = ClassLoader.getSystemResourceAsStream("META-INF/MANIFEST.MF");
        try {
            return new Manifest(stream).getMainAttributes().getValue("Implementation-Version");
        } catch (IOException | NullPointerException e) {
            log.catching(e);
        }
        return "unknown-version";
    }

    private static Properties getProperties(String arg) throws IOException {
        String configFileName = System.getenv("CC_CONFIG_FILE");
        if (arg != null) configFileName = arg;
        if (configFileName == null) configFileName = "config/clustercode.properties";
        log.info("Reading configuration file {}...", configFileName);
        return ConfigurationHelper.loadPropertiesFromFile(configFileName);
    }

}
