package net.chrigel.clustercode;

import net.chrigel.clustercode.api.RestApiServices;
import net.chrigel.clustercode.statemachine.StateMachineService;
import net.chrigel.clustercode.util.ConfigurationHelper;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

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

        GuiceContainer container = new GuiceContainer(config);

        container.getInstance(RestApiServices.class).start();
        container.getInstance(StateMachineService.class).initialize();
    }

    private static Properties getProperties(String arg) throws IOException {
        String configFileName = System.getenv("CC_CONFIG_FILE");
        if (arg != null) configFileName = arg;
        if (configFileName == null) configFileName = "config/clustercode.properties";
        log.info("Reading configuration file {}...", configFileName);
        return ConfigurationHelper.loadPropertiesFromFile(configFileName);
    }

}
