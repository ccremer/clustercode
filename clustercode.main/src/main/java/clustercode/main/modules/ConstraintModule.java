package clustercode.main.modules;

import clustercode.api.config.ConfigLoader;
import clustercode.api.domain.Constraint;
import clustercode.impl.constraint.*;
import clustercode.impl.util.InvalidConfigurationException;
import clustercode.impl.util.di.ModuleHelper;
import com.google.inject.multibindings.Multibinder;

import java.util.HashMap;
import java.util.Map;

public class ConstraintModule extends ConfigurableModule {

    public ConstraintModule(ConfigLoader loader) {
        super(loader);
    }

    @Override
    protected void configure() {
        ConstraintConfig config = loader.getConfig(ConstraintConfig.class);
        bind(ConstraintConfig.class).toInstance(config);

        var setBinder = Multibinder.newSetBinder(binder(), Constraint.class);
        var map = getConstraintMap();

        try {
            ModuleHelper.verifyIn(config.active_constraints())
                        .that(Constraints.ALL)
                        .isNotGivenTogetherWith(Constraints.NONE);
        } catch (InvalidConfigurationException ex) {
            addError(ex);
        }

        if (config.active_constraints().contains(Constraints.ALL)) {
            map.forEach((key, value) -> setBinder.addBinding().to(value));
        } else if (config.active_constraints().contains(Constraints.NONE)) {
            setBinder.addBinding().to(NoConstraint.class);
        } else {
            config.active_constraints().forEach(key -> setBinder.addBinding().to(map.get(key)));
        }

    }

    private Map<Constraints, Class<? extends Constraint>> getConstraintMap() {
        Map<Constraints, Class<? extends Constraint>> map = new HashMap<>();
        map.put(Constraints.FILE_NAME, FileNameConstraint.class);
        map.put(Constraints.TIME, TimeConstraint.class);
        map.put(Constraints.FILE_SIZE, FileSizeConstraint.class);
        map.put(Constraints.CLUSTER, ClusterConstraint.class);
        return map;
    }
}
