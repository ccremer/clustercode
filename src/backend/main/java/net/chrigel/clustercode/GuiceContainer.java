package net.chrigel.clustercode;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.api.impl.ApiModule;
import net.chrigel.clustercode.cleanup.impl.CleanupModule;
import net.chrigel.clustercode.cluster.impl.ClusterModule;
import net.chrigel.clustercode.constraint.impl.ConstraintModule;
import net.chrigel.clustercode.process.impl.ProcessModule;
import net.chrigel.clustercode.scan.impl.ScanModule;
import net.chrigel.clustercode.statemachine.actions.ActionModule;
import net.chrigel.clustercode.statemachine.states.StateMachineModule;
import net.chrigel.clustercode.transcode.impl.TranscodeModule;
import net.chrigel.clustercode.util.di.EnvironmentModule;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.jar.Manifest;

@XSlf4j
public class GuiceContainer {

    private final Injector injector;

    public GuiceContainer(Properties config) {
        log.debug("Creating guice modules...");
        List<Module> modules = new LinkedList<>();

        modules.add(new GlobalModule());
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
        injector = Guice.createInjector(modules);
    }

    public <T> T getInstance(Class<T> clazz) {
        return injector.getInstance(clazz);
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

}
