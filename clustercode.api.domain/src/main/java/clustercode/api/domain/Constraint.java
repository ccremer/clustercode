package clustercode.api.domain;

/**
 * Represents a matcher with which a media candidate can be excluded or included for job scheduling. The implementing
 * class should throw a runtime exception in the constructor if there is a configuration error. The order of
 * constraints is unspecified if there are more than one. As soon as one constraint returns false, the candidate is
 * being excluded from scheduling.
 */
public interface Constraint {

    /**
     * Tests whether the given candidate is viable for scheduling. The parameter must not be modified
     * (non-interfering). If the constraint encountered an error, a warning is being logged and false is returned.
     *
     * @param candidate the candidate to test, not null.
     * @return true if viable for scheduling, false otherwise.
     */
    boolean accept(Media candidate);

}
