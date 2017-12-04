package net.chrigel.clustercode.constraint.impl;

import com.google.inject.multibindings.Multibinder;
import net.chrigel.clustercode.constraint.Constraint;
import net.chrigel.clustercode.util.di.AbstractPropertiesModule;
import net.chrigel.clustercode.util.di.ModuleHelper;

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

    /**
     * Defines the key for the enabled constraints.
     */
    public static final String CONSTRAINT_STRATEGIES_KEY = "CC_CONSTRAINTS_ACTIVE";

    private Properties properties;

    public ConstraintModule(Properties properties) {
        this.properties = properties;
    }

    @Override
    protected void configure() {
        Multibinder<Constraint> constraintBinder = Multibinder.newSetBinder(binder(), Constraint.class);
        String none = "NONE";
        String constraints = getEnvironmentVariableOrPropertyIgnoreError(properties, CONSTRAINT_STRATEGIES_KEY, none);
        if ("ALL".equalsIgnoreCase(constraints.trim())) {
            ModuleHelper.bindAll(constraintBinder, Constraints::values);
        } else if (none.equalsIgnoreCase(constraints.trim())) {
            ModuleHelper.bindStrategies(constraintBinder, none, Constraints::valueOf);
        } else {
            try {
                ModuleHelper.bindStrategies(constraintBinder, constraints.replace(none, ""), Constraints::valueOf);
            } catch (IllegalArgumentException ex) {
                addError(ex);
            }
        }
    }

}
