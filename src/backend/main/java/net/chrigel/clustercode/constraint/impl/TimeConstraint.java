package net.chrigel.clustercode.constraint.impl;

import com.google.inject.name.Named;
import net.chrigel.clustercode.scan.Media;
import net.chrigel.clustercode.util.InvalidConfigurationException;

import javax.inject.Inject;
import java.time.Clock;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Provides an implementation of a time constraint. The media will not be accepted for scheduling when the current
 * time is outside of the configurable 24h time window. The 'begin' and 'stop' strings are expected in the "HH:mm"
 * format (0-23).
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
        try {
            this.begin = LocalTime.parse(begin, formatter);
            this.stop = LocalTime.parse(stop, formatter);
        } catch (DateTimeParseException ex) {
            throw new InvalidConfigurationException("The time format is HH:mm. You specified: begin({}), stop({})", ex,
                    begin, stop);
        }
        checkConfiguration();
    }

    private void checkConfiguration() {
        if (begin.compareTo(stop) == 0) {
            throw new InvalidConfigurationException("Begin and stop time are identical (specify different times in " +
                    "HH:mm format).");
        }
    }

    @Override
    public boolean accept(Media candidate) {
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
