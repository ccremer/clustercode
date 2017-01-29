package net.chrigel.clustercode;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.workflow.StateController;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

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
            log.error("Configuration file not provided in arguments. Exiting...");
        }
        String configFileName = args[0];

        log.debug("Creating guice modules...");
        List<Module> modules = new LinkedList<>();

      //  modules.add(new AbstractPropertiesModule(configFileName));
        //modules.add(new AbstractPropertiesModule(commonConfigFile.getSourcePath()));
        //modules.add(new AbstractPropertiesModule(workflowConfigFile.getSourcePath()));
        //modules.add(new SettingsModule());
        //modules.add(new MediaModule());
        //modules.add(new ProcessModule());
        //modules.add(getTranscoderModule(nodeConfig));
        //modules.add(getWorkflowModule(loadWorkflowConfiguration(nodeConfig, configDir)));

        log.info("Booting application...");
        Injector injector = Guice.createInjector(modules);

        injector.getInstance(StateController.class).initialize();
    }

}
