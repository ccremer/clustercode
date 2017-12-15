package net.chrigel.clustercode;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import java.time.Clock;

public class GlobalModule extends AbstractModule {

    @Override
    protected void configure() {

    }

    @Provides
    private Clock getSystemClock() {
        return Clock.systemDefaultZone();
    }
}
