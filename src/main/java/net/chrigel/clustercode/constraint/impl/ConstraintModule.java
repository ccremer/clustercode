package net.chrigel.clustercode.constraint.impl;

import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import net.chrigel.clustercode.constraint.Constraint;
import net.chrigel.clustercode.util.AbstractPropertiesModule;
import net.chrigel.clustercode.util.ConfigurationHelper;

import java.io.IOException;
import java.time.Clock;
import java.util.Properties;

public class ConstraintModule extends AbstractPropertiesModule {
    private String fileName;

    public ConstraintModule(String fileName) {
        this.fileName = fileName;
    }

    @Override
    protected void configure() {
        Multibinder<Constraint> constraintBinder = Multibinder.newSetBinder(binder(), Constraint.class);
        constraintBinder.addBinding().to(FileSizeConstraint.class);
        constraintBinder.addBinding().to(TimeConstraint.class);

        try {
            Properties properties = ConfigurationHelper.loadPropertiesFromFile(fileName);
            bindEnvironmentVariablesWithDefaultsByObject(properties);
        } catch (IOException e) {
            addError(e);
        }
    }

    @Provides
    private Clock getSystemClock() {
        return Clock.systemDefaultZone();
    }
}
