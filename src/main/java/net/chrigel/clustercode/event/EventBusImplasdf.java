package net.chrigel.clustercode.event;

import java.util.function.Consumer;

public class EventBusImplasdf<B> implements EventBus<B> {

    @Override
    public <E extends B> void registerEventHandler(Class<? extends B> clazz, Consumer<Event<E>> subscriber) {

    }

    @Override
    public <E extends B> void unRegister(Class<? extends B> clazz, Consumer<Event<E>> subscriber) {

    }

    @Override
    public <E extends B> void emit(Event<E> event) {

    }
}
