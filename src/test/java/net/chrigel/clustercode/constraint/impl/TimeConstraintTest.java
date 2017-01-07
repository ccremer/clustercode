package net.chrigel.clustercode.constraint.impl;

import org.junit.Before;
import org.junit.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class TimeConstraintTest {

    private TimeConstraint subject;

    private Clock fixedClock;

    @Before
    public void setUp() throws Exception {

        fixedClock = Clock.fixed(LocalDateTime.of(2017, 1, 1, 13, 27).toInstant(ZoneOffset.UTC),
                ZoneId.systemDefault());

        subject = new TimeConstraint();
    }

    @Test
    public void accept() throws Exception {

    }

}