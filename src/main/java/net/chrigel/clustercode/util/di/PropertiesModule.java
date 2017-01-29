package net.chrigel.clustercode.util.di;


import com.google.inject.name.Names;
import net.chrigel.clustercode.util.ConfigurationHelper;

import java.io.IOException;
import java.util.Properties;

/**
 * Provides a guice module which loads and applies settings from an external properties file.
 */
public class PropertiesModule extends AbstractPropertiesModule {

    private String fileName;
    private Properties properties;

    /**
     * Creates the module with the specified file name. The properties will be loaded via
     * {@code ClassLoader.getSystemResourceAsStream(fileName);}. If the file is not in the classpath, it will be
     * searched on the file system.
     *
     * @param fileName the file name accessible by the ClassLoader.
     */
    public PropertiesModule(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Creates the module with the specified properties, if the
     *
     * @param properties
     */
    public PropertiesModule(Properties properties) {
        this.properties = properties;
    }

    @Override
    protected final void configure() {
        try {
            if (properties == null) {
                properties = ConfigurationHelper.loadPropertiesFromFile(fileName);
            }
            Names.bindProperties(binder(), properties);
        } catch (IOException e) {
            addError(e);
        }
    }

}