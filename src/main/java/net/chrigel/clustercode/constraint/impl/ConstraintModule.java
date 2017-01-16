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

    /**
     * Defines the key for the file size constraint (min size)
     */
    public static final String CONSTRAINT_FILESIZE_MIN_SIZE_KEY = "CC_CONSTRAINT_FILE_MIN_SIZE";
    /**
     * Defines the key for the file size constraint (max size)
     */
    public static final String CONSTRAINT_FILESIZE_MAX_SIZE_KEY = "CC_CONSTRAINT_FILE_MAX_SIZE";

    /**
     * Defines the key for the time constraint (begin time)
     */
    public static final String CONSTRAINT_TIME_BEGIN_KEY = "CC_CONSTRAINT_TIME_BEGIN";

    /**
     * Defines the key for the time constraint (stop time)
     */
    public static final String CONSTRAINT_TIME_STOP_KEY = "CC_CONSTRAINT_TIME_STOP";

    /**
     * Defines the key for the file name constraint (regex)
     */
    public static final String CONSTRAINT_FILENAME_REGEX_KEY = "CC_CONSTRAINT_FILE_REGEX";

    private String fileName;

    public ConstraintModule(String fileName) {
        this.fileName = fileName;
    }

    @Override
    protected void configure() {
        Multibinder<Constraint> constraintBinder = Multibinder.newSetBinder(binder(), Constraint.class);
        constraintBinder.addBinding().to(FileSizeConstraint.class);
        constraintBinder.addBinding().to(TimeConstraint.class);
        constraintBinder.addBinding().to(FileNameConstraint.class);

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
