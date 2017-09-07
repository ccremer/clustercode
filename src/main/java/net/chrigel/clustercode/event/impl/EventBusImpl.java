package net.chrigel.clustercode.event.impl;

import lombok.Synchronized;
import lombok.extern.slf4j.XSlf4j;
import lombok.val;
import net.chrigel.clustercode.event.Event;
import net.chrigel.clustercode.event.EventBus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

@XSlf4j
public class EventBusImpl<B> implements EventBus<B> {

    private final HashMap<Class, List<Consumer>> map = new HashMap<>();

    @Synchronized
    @Override
    public <E extends B> void registerEventHandler(Class<? extends B> clazz, Consumer<Event<E>> subscriber) {
        if (map.containsKey(clazz)) {
            val list = map.get(clazz);
            if (list.contains(subscriber)) return;
            list.add(subscriber);
        } else {
            this.map.put(clazz, Arrays.asList(subscriber));
        }
        log.debug("Registered {} for {}", subscriber, clazz.getName());
    }

    @Synchronized
    @Override
    public <E extends B> void unRegister(Class<? extends B> clazz, Consumer<Event<E>> subscriber) {
        if (!map.containsKey(clazz)) return;
        map.get(clazz).remove(subscriber);
    }

    @Synchronized
    @Override
    @SuppressWarnings("unchecked")
    public <E extends B> void emit(Event<E> event) {
        Class clazz = event.getPayload().getClass();
        if (!map.containsKey(clazz)) return;
        log.debug("Emitting event: {}", event);
        map.get(clazz).forEach(consumer -> consumer.accept(event));
    }

}
