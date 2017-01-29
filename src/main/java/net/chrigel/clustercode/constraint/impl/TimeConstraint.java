package net.chrigel.clustercode.constraint.impl;

import com.google.inject.name.Named;
import net.chrigel.clustercode.task.Media;

import javax.inject.Inject;
import java.time.Clock;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Provides an implementation of a time constraint. The media will not be accepted for scheduling when the current
 * time is outside of the configurable 24h time window. The 'begin' and 'stop' strings are expected in the "HH:mm"
 * format (0-23). If either value is "-1", then this constraint becomes disabled, accepting all candidates.
 */
class TimeConstraint
        extends AbstractConstraint {

    private final Clock clock;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
    private LocalTime stop;
    private LocalTime begin;

    @Inject
    protected TimeConstraint(@Named(ConstraintModule.CONSTRAINT_TIME_BEGIN_KEY) String begin,
                             @Named(ConstraintModule.CONSTRAINT_TIME_STOP_KEY) String stop,
                             Clock clock) {
        this.clock = clock;
        setEnabled(!("-1".equals(begin) || "-1".equals(stop)));
        if (isEnabled()) {
            this.begin = LocalTime.parse(begin, formatter);
            this.stop = LocalTime.parse(stop, formatter);
            if (begin.compareTo(stop) == 0) {
                throw new IllegalArgumentException("Begin and stop time are identical. Specify \"-1\" to disable.");
            }
        }
    }

    @Override
    public boolean acceptCandidate(Media candidate) {
        LocalTime now = LocalTime.now(clock);
        if (begin.isBefore(stop)) {
            return logAndReturn(begin.isBefore(now) && now.isBefore(stop), now);
        } else {
            return logAndReturn((
                    now.isAfter(begin) && now.isAfter(stop)) || (now.isBefore(stop) && now.isBefore(begin)), now);
        }
    }

    protected boolean logAndReturn(boolean result, LocalTime now) {
        return logAndReturnResult(result, "Time window {} (begin: {}, stop {})",
                    formatter.format(now), formatter.format(begin), formatter.format(stop));
    }

}
