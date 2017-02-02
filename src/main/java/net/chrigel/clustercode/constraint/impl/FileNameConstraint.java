package net.chrigel.clustercode.constraint.impl;

import net.chrigel.clustercode.task.Media;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.regex.Pattern;

/**
 * Provides a constraint which enables file name checking by regex. The input path of the candidate is being
 * checked (relative, without base input directory). Specify a Java-valid regex pattern, otherwise a runtime
 * exception is being thrown.
 */
class FileNameConstraint
        extends AbstractConstraint {

    private final Pattern pattern;

    @Inject
    FileNameConstraint(@Named(ConstraintModule.CONSTRAINT_FILENAME_REGEX_KEY) String regex) {
        this.pattern = Pattern.compile(regex);
    }

    @Override
    public boolean accept(Media candidate) {
        String toTest = candidate.getSourcePath().toString();
        return logAndReturnResult(pattern.matcher(toTest).matches(), "file name of {} with regex {}",
                candidate.getSourcePath(), pattern.pattern());
    }

}
