package net.chrigel.clustercode.util;


import com.google.inject.name.Names;

import java.io.IOException;

/**
 * Provides a guice module which loads and applies settings from an external properties file.
 */
public class PropertiesModule extends AbstractPropertiesModule {

    private final String fileName;

    /**
     * Creates the module with the specified file name. The properties will be loaded via
     * {@code ClassLoader.getSystemResourceAsStream(fileName);}. If the file is not in the classpath, it will be
     * searched on the file system.
     *
     * @param fileName the file name accessible by the ClassLoader.
     */
    public PropertiesModule(final String fileName) {
        this.fileName = fileName;
    }

    @Override
    protected final void configure() {
        try {
            Names.bindProperties(binder(), ConfigurationHelper.loadPropertiesFromFile(fileName));
        } catch (IOException e) {
            addError(e);
        }
    }

}