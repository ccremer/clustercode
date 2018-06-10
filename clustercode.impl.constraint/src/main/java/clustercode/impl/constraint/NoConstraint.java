package clustercode.impl.constraint;

import clustercode.api.domain.Media;

public class NoConstraint extends AbstractConstraint {

    @Override
    public boolean accept(Media candidate) {
        return logAndReturnResult(true, "{}", candidate);
    }
}
