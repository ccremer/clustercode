package net.chrigel.clustercode.constraint.impl;

import net.chrigel.clustercode.constraint.Constraint;
import net.chrigel.clustercode.util.di.EnumeratedImplementation;

public enum Constraints implements EnumeratedImplementation<Constraint> {

    FILE_NAME(FileNameConstraint.class),
    TIME(TimeConstraint.class),
    FILE_SIZE(FileSizeConstraint.class),
    NONE(NoConstraint.class);

    private final Class<? extends Constraint> implementationClass;

    Constraints(Class<? extends Constraint> implementationClass) {
        this.implementationClass = implementationClass;
    }

    @Override
    public Class<? extends Constraint> getImplementingClass() {
        return implementationClass;
    }

}
