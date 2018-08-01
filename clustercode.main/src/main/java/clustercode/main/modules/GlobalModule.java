package clustercode.main.modules;

import clustercode.api.event.RxEventBus;
import clustercode.api.event.RxEventBusImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import java.time.Clock;

public class GlobalModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(RxEventBus.class).to(RxEventBusImpl.class).in(Singleton.class);
    }

    @Provides
    private Clock getSystemClock() {
        return Clock.systemDefaultZone();
    }
}
