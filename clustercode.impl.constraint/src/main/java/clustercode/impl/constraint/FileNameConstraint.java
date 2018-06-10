package clustercode.impl.constraint;

import clustercode.api.domain.Media;

import javax.inject.Inject;
import java.util.regex.Pattern;

/**
 * Provides a constraint which enables file name checking by regex. The input path of the candidate is being
 * checked (relative, without base input directory). Specify a Java-valid regex pattern, otherwise a runtime
 * exception is being thrown.
 */
public class FileNameConstraint
        extends AbstractConstraint {

    private final Pattern pattern;

    @Inject
    FileNameConstraint(ConstraintConfig config) {
        this.pattern = Pattern.compile(config.filename_regex());
    }

    @Override
    public boolean accept(Media candidate) {
        String toTest = candidate.getSourcePath().toString();
        return logAndReturnResult(pattern.matcher(toTest).matches(), "file name of {} with regex {}",
                candidate.getSourcePath(), pattern.pattern());
    }

}
