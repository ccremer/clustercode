package net.chrigel.clustercode.constraint.impl;

import net.chrigel.clustercode.task.MediaCandidate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class TimeConstraintTest {

    private TimeConstraint subject;

    @Mock
    private MediaCandidate candidate;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void accept_ShouldReturnTrue_IfConstraintDisabled() throws Exception {
        subject = new TimeConstraint("-1", "-1", Clock.systemDefaultZone());

        assertThat(subject.accept(candidate)).isTrue();
    }

    @Test
    public void accept_ShouldReturnTrue_IfCurrentTimeIsBetweenBeginAndStop() throws Exception {
        subject = new TimeConstraint("13:00", "14:00",
                Clock.fixed(Instant.parse("2017-01-01T13:30:00Z"), ZoneOffset.UTC));

        assertThat(subject.accept(candidate)).isTrue();
    }

    @Test
    public void accept_ShouldReturnFalse_IfCurrentTimeIsBeforeBegin() throws Exception {
        subject = new TimeConstraint("13:00", "14:00",
                Clock.fixed(Instant.parse("2017-01-01T12:30:00Z"), ZoneOffset.UTC));

        assertThat(subject.accept(candidate)).isFalse();
    }

    @Test
    public void accept_ShouldReturnFalse_IfCurrentTimeIsAfterStop() throws Exception {
        subject = new TimeConstraint("13:00", "14:00",
                Clock.fixed(Instant.parse("2017-01-01T14:30:00Z"), ZoneOffset.UTC));

        assertThat(subject.accept(candidate)).isFalse();
    }

    @Test
    public void accept_ShouldReturnTrue_IfCurrentTimeIsAfterBegin_AndStopIsBeforeBegin() throws Exception {
        subject = new TimeConstraint("13:00", "11:00",
                Clock.fixed(Instant.parse("2017-01-01T14:30:00Z"), ZoneOffset.UTC));

        assertThat(subject.accept(candidate)).isTrue();
    }

    @Test
    public void accept_ShouldReturnFalse_IfCurrentTimeIsBeforeBegin_AndStopIsBeforeBegin() throws Exception {
        subject = new TimeConstraint("13:00", "11:00",
                Clock.fixed(Instant.parse("2017-01-01T12:30:00Z"), ZoneOffset.UTC));

        assertThat(subject.accept(candidate)).isFalse();
    }

    @Test
    public void ctor_ShouldThrowException_IfBeginAndStopAreSame() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
                subject = new TimeConstraint("12:00", "12:00",
                        Clock.fixed(Instant.parse("2017-01-01T12:30:00Z"), ZoneOffset.UTC)));
    }

}