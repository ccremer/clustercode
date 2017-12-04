package net.chrigel.clustercode.event.impl;

import lombok.Synchronized;
import lombok.extern.slf4j.XSlf4j;
import lombok.val;
import net.chrigel.clustercode.event.Event;
import net.chrigel.clustercode.event.EventBus;
import net.chrigel.clustercode.util.OptionalFunction;
import net.chrigel.clustercode.util.UnsafeCastUtil;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@XSlf4j
public class EventBusImpl<B extends Serializable> implements EventBus<B> {

    private final HashMap<Class, List<OptionalFunction>> map = new HashMap<>();

    @Synchronized
    @Override
    public <E extends B, R> void registerEventHandler(Class<? extends B> clazz, OptionalFunction<Event<E>, R> subscriber) {
        if (map.containsKey(clazz)) {
            val list = map.get(clazz);
            if (list.contains(subscriber)) {
                log.debug("Already registered: {}", subscriber);
                return;
            }
            list.add(subscriber);
        } else {
            this.map.put(clazz, new LinkedList<>(Collections.singletonList(subscriber)));
        }
        log.debug("Registered: {} for {}", subscriber, clazz.getName());
    }

    @Synchronized
    @Override
    public <E extends B, R> void unRegister(Class<? extends B> clazz, OptionalFunction<Event<E>, R> subscriber) {
        if (!map.containsKey(clazz)) return;
        List<OptionalFunction> responders = map.get(clazz);
        responders.remove(subscriber);
        if (responders.isEmpty()) map.remove(clazz);
        log.debug("Removed: {} for {}", subscriber, clazz);
    }

    @Synchronized
    @Override
    @SuppressWarnings("unchecked")
    public <E extends B> void emit(Event<E> event) {
        Class clazz = event.getPayload().getClass();
        if (!map.containsKey(clazz)) return;
        log.debug("Emitting event: {}", event);
        map.get(clazz).forEach(consumer -> consumer.apply(event));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends B> CompletableFuture<Void> emitAsync(Event<E> event) {
        Class clazz = event.getPayload().getClass();
        if (!map.containsKey(clazz)) return CompletableFuture.runAsync(() ->
            log.trace("No handlers for {}", event));
        log.debug("Emitting: {}", event);
        return CompletableFuture.runAsync(() -> map.get(clazz).forEach(consumer -> consumer.apply(event)));
    }

    @Override
    public <R, E extends B> Optional<R> emitAndGet(Event<E> event) {
        return UnsafeCastUtil.cast(emitAndGetAll(event).stream().findFirst());
    }

    @Override
    public <R, E extends B> Collection<R> emitAndGetAll(Event<E> event) {
        Class clazz = event.getPayload().getClass();
        if (!map.containsKey(clazz)) return Collections.emptyList();
        log.debug("Emitting: {}", event);

        List<OptionalFunction<Event<E>, R>> responders = UnsafeCastUtil.cast(map.get(clazz));
        return responders.stream()
            .map(responder -> responder.apply(event))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }

}
