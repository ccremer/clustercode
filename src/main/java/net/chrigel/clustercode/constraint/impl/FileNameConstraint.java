package net.chrigel.clustercode.constraint.impl;

import net.chrigel.clustercode.task.MediaCandidate;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.regex.Pattern;

/**
 * Provides a constraint which enables file name checking by regex. The input path of the candidate is being
 * checked (relative, without base input directory). Specify a Java-valid regex pattern, otherwise a runtime
 * exception is being thrown. Specify ":" (without quotes) to disable the constraint.
 */
class FileNameConstraint
        extends AbstractConstraint {

    private Pattern pattern;

    @Inject
    FileNameConstraint(@Named(ConstraintConfiguration.CONSTRAINT_FILENAME_REGEX_KEY) String regex) {
        setEnabled(!":".equals(regex));
        if (isEnabled()) {
            this.pattern = Pattern.compile(regex);
        }
    }

    @Override
    protected boolean acceptCandidate(MediaCandidate candidate) {
        String toTest = candidate.getSourcePath().toString();
        return pattern.matcher(toTest).matches();
    }

}
