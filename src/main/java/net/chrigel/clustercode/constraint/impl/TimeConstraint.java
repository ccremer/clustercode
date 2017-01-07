package net.chrigel.clustercode.constraint.impl;

import com.google.inject.name.Named;
import net.chrigel.clustercode.constraint.Constraint;
import net.chrigel.clustercode.task.MediaCandidate;

import javax.inject.Inject;
import java.time.Clock;
import java.time.LocalTime;

/**
 *
 */
public class TimeConstraint
        implements Constraint {

    private final int beginHour;
    private final int beginMinute;
    private final int stopHour;
    private final int stopMinute;
    private final Clock clock;
    private boolean enabled;

    /**
     * Creates a new time constraint.
     *
     * @param begin a string in the format "hh:mm". Set to "-1" if not enabled.
     * @param stop  a string in the format "hh:mm". Set to "-1" if not enabled.
     */
    @Inject
    TimeConstraint(@Named(ConstraintConfiguration.CONSTRAINT_TIME_BEGIN_KEY) String begin,
                   @Named(ConstraintConfiguration.CONSTRAINT_TIME_STOP_KEY) String stop,
                   Clock clock) {
        this.clock = clock;
        if ("-1".equals(begin) || "-1".equals(stop)) {
            this.enabled = true;
            this.beginHour = -1;
            this.beginMinute = -1;
            this.stopHour = -1;
            this.stopMinute = -1;
        } else {

            this.beginHour = Integer.valueOf(begin.split(":")[0]);
            this.beginMinute = Integer.valueOf(begin.split(":")[1]);
            this.stopHour = Integer.valueOf(stop.split(":")[0]);
            this.stopMinute = Integer.valueOf(stop.split(":")[1]);
        }
    }

    @Override
    public boolean accept(MediaCandidate candidate) {
        if (enabled) {
            LocalTime begin = LocalTime.of(beginHour, beginMinute);
            LocalTime stop = LocalTime.of(stopHour, stopMinute);
            LocalTime now = LocalTime.now(clock);
            if (begin.compareTo(stop) == 0) {
                return true;
            }
            if (begin.isBefore(stop)) {
                return begin.isBefore(now) && now.isBefore(stop);
            } else {
                return (now.isAfter(begin) && now.isAfter(stop)) || (now.isBefore(stop) && now.isBefore(begin));
            }
        } else {
            return true;
        }

    }

}
