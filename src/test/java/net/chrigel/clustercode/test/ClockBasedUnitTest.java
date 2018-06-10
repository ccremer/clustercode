package net.chrigel.clustercode.test;

import java.time.*;
import java.util.Locale;

public interface ClockBasedUnitTest {

    /**
     * Gets a fixed clock which uses the given instant and the system default zone.
     *
     * @param time the instant.
     * @return a fixed clock which always returns the given instant.
     */
    default Clock getFixedClock(Instant time) {
        return Clock.fixed(time, ZoneId.systemDefault());
    }

    /**
     * Gets a fixed clock in {@link ZoneOffset#UTC} with the given date.
     *
     * @param year   the year.
     * @param month  the month (1..12)
     * @param day    the day (1..31)
     * @param hour   the hour of day (0..23)
     * @param minute the minute of hour (0..59)
     * @param second the second of minute (0..59)
     * @return a fixed clock which always returns the given time.
     * @throws java.time.format.DateTimeParseException if the time cannot be parsed using the given parameters.
     */
    default Clock getFixedClock(int year, int month, int day, int hour, int minute, int second) {
        return getFixedClock(year, month, day, hour, minute, second, ZoneOffset.UTC);
    }

    /**
     * Gets a fixed clock in {@link ZoneOffset#UTC} with the given date.
     *
     * @param year   the year.
     * @param month  the month (1..12)
     * @param day    the day (1..31)
     * @param hour   the hour of day (0..23)
     * @param minute the minute of hour (0..59)
     * @return a fixed clock which always returns the given time.
     * @throws java.time.format.DateTimeParseException if the time cannot be parsed using the given parameters.
     */
    default Clock getFixedClock(int year, int month, int day, int hour, int minute) {
        return getFixedClock(year, month, day, hour, minute, 0, ZoneOffset.UTC);
    }

    /**
     * Gets a fixed clock in {@link ZoneOffset#UTC} with the given date. The date will be 2017-01-31.
     *
     * @param hour   the hour of day (0..23)
     * @param minute the minute of hour (0..59)
     * @return a fixed clock which always returns the given time.
     * @throws java.time.format.DateTimeParseException if the time cannot be parsed using the given parameters.
     */
    default Clock getFixedClock(int hour, int minute) {
        return getFixedClock(2017, 1, 31, hour, minute, 0, ZoneOffset.UTC);
    }

    /**
     * Gets a fixed clock with the given date and zone.
     *
     * @param year   the year.
     * @param month  the month (1..12)
     * @param day    the day (1..31)
     * @param hour   the hour of day (0..23)
     * @param minute the minute of hour (0..59)
     * @param second the second of minute (0..59)
     * @param zone   the zone id.
     * @return a fixed clock which always returns the given time.
     */
    default Clock getFixedClock(int year, int month, int day, int hour, int minute, int second, ZoneOffset zone) {
        return Clock.fixed(getLocalTime(year, month, day, hour, minute, second).toInstant(zone), zone);
    }

    /**
     * Gets a local zoned date time with the given parameters.
     *
     * @param year   the year.
     * @param month  the month (1..12)
     * @param day    the day (1..31)
     * @param hour   the hour of day (0..23)
     * @param minute the minute of hour (0..59)
     * @param second the second of minute (0..59)
     * @return the time.
     * @throws java.time.format.DateTimeParseException if the instant cannot be created from the given parameters.
     */
    default LocalDateTime getLocalTime(int year, int month, int day, int hour, int minute, int second) {
        return LocalDateTime.parse(new StringBuilder()
                .append(year).append('-').append(preZero(month)).append('-').append(preZero(day))
                .append('T')
                .append(preZero(hour)).append(':').append(preZero(minute)).append(':').append(preZero(second))
                .toString());
    }

    /**
     * Gets a local zoned date time with the given parameters.
     *
     * @param year   the year.
     * @param month  the month (1..12)
     * @param day    the day (1..31)
     * @param hour   the hour of day (0..23)
     * @param minute the minute of hour (0..59)
     * @return the time.
     * @throws java.time.format.DateTimeParseException if the instant cannot be created from the given parameters.
     */
    default LocalDateTime getLocalTime(int year, int month, int day, int hour, int minute) {
        return getLocalTime(year, month, day, hour, minute, 0);
    }

    /**
     * Gets a local zoned date time with the given parameters.
     *
     * @param year  the year.
     * @param month the month (1..12)
     * @param day   the day (1..31)
     * @return the time.
     * @throws java.time.format.DateTimeParseException if the instant cannot be created from the given parameters.
     */
    default LocalDateTime getLocalTime(int year, int month, int day) {
        return getLocalTime(year, month, day, 0, 0, 0);
    }

    /**
     * Gets an instant with the given time. The date will be 2017-01-31.
     *
     * @param hour   the hour of day (0..23)
     * @param minute the minute of hour (0..59)
     * @return the instant.
     * @throws java.time.format.DateTimeParseException if the instant cannot be created from the given parameters.
     */
    default LocalDateTime getLocalTime(int hour, int minute) {
        return getLocalTime(2017, 1, 31, hour, minute);
    }

    /**
     * Returns a string that represents the given number, but a single zero is prepended if the number is {@literal <
     * 10}. Example: returns "08" if number is 8. Returns "12" if number is 12.
     *
     * @param number
     * @return the string representation.
     */
    default String preZero(int number) {
        return preZero(number, 2);
    }

    /**
     * Returns a string that represents the given number, but is prepended with zeros until the digits reach {@code
     * length}. Example: returns "007" if number is 7 and length is 3. Returns "012" if number is 12 and length is 3.
     *
     * @param number
     * @param length
     * @return the string representation.
     */
    default String preZero(int number, int length) {
        return String.format(Locale.ENGLISH, "%0".concat(Integer.toString(length)).concat("d"), number);
    }
}
