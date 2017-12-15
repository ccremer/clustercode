package net.chrigel.clustercode.util.di;

import net.chrigel.clustercode.util.ConfigurationHelper;

import java.io.IOException;
import java.util.Properties;

public class EnvironmentModule extends AbstractPropertiesModule {

    private Properties properties;
    private String fileName;

    public EnvironmentModule(String fileName) {
        this.fileName = fileName;
    }

    public EnvironmentModule(Properties properties) {
        this.properties = properties;
    }

    @Override
    protected void configure() {
        try {
            if (properties == null) {
                properties = ConfigurationHelper.loadPropertiesFromFile(fileName);
            }
            bindEnvironmentVariablesWithDefaultsByObject(properties);
        } catch (IOException e) {
            addError(e);
        }
    }
}
