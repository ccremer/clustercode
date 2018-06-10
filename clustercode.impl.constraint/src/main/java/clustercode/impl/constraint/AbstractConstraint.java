package clustercode.impl.constraint;

import clustercode.api.domain.Constraint;
import clustercode.api.domain.Media;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

/**
 * Provides a template class for constraints. A {@link XLogger} is being configured, accessible with {@code log}
 * field.
 */
public abstract class AbstractConstraint implements Constraint {

    protected final XLogger log = XLoggerFactory.getXLogger(getClass());

    protected AbstractConstraint() {
        log.info("Enabled {}.", getClass().getSimpleName());
    }

    /**
     * Logs a debug message and returns the result unmodified. This method can be used before returning from
     * {@link #accept(Media)}. "Accepted: " or "Declined: " will be prepended before the {@code formatString}, based on
     * {@code accepted}.
     *
     * @param accepted     the result of {@link #accept(Media)}.
     * @param formatString The format string to log. Use {} as placeholder for variables (SLF4J syntax).
     * @param arguments    the arguments for {@code formatString}
     * @return {@code accepted}
     */
    protected final boolean logAndReturnResult(boolean accepted, String formatString, Object... arguments) {
        if (log.isDebugEnabled()) {
            String decision;
            if (accepted) {
                decision = "Accepted: ";
            } else {
                decision = "Declined: ";
            }
            log.debug(decision.concat(formatString), arguments);
        }
        return accepted;
    }

}
