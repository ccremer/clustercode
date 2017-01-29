package net.chrigel.clustercode.constraint.impl;

import net.chrigel.clustercode.constraint.Constraint;
import net.chrigel.clustercode.task.Media;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

/**
 * Provides a template class for constraints. A {@link XLogger} is being configured, accessible with {@code log}
 * field. Enable the constraint with {@link #setEnabled(boolean)} if the constraint should be used (default is false,
 * which would accept any media candidates).
 */
public abstract class AbstractConstraint implements Constraint {

    protected final XLogger log = XLoggerFactory.getXLogger(getClass());
    private boolean enabled;

    protected AbstractConstraint() {
        this.enabled = false;
    }

    @Override
    public final boolean accept(Media candidate) {
        return !enabled || acceptCandidate(candidate);
    }

    /**
     * See {@link Constraint#accept(Media)}, except that the check for enabling can be omitted. This method
     * will not be called if the abstract constraint is disabled.
     *
     * @param candidate the candidate.
     * @return true if candidate should be accepted by this constraint matcher.
     */
    protected abstract boolean acceptCandidate(Media candidate);

    /**
     * Returns the boolean indicating whether this constraint is enabled. By default the constraint is disabled.
     *
     * @return
     */
    protected final boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the enabled flag of the constraint. An info statement will be logged.
     *
     * @param enabled true if enabled.
     */
    protected final void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            log.debug("Enabling constraint.");
        } else {
            log.debug("Disabling constraint.");
        }

    }

    /**
     * Logs a debug message and returns the result unmodified. This method can be used before returning from
     * {@link #acceptCandidate(Media)}. Before the log line, "Accepted: " or "Declined: " will be prepended
     * before the {@code formatString}, based on {@code accepted}.
     *
     * @param accepted     the result of {@link #acceptCandidate(Media)}.
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
