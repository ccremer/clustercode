package net.chrigel.clustercode.event;

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
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@XSlf4j
public class EventBusImpl<B extends Serializable> implements EventBus<B> {

    private final HashMap<Class, List<Consumer>> map = new HashMap<>();

    @Synchronized
    @Override
    public <E extends B> void registerEventHandler(Class<? extends B> clazz, Consumer<Event<E>> subscriber) {
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
    public <E extends B> void unRegister(Class<? extends B> clazz, Consumer<Event<E>> subscriber) {
        if (!map.containsKey(clazz)) return;
        List<Consumer> responders = map.get(clazz);
        responders.remove(subscriber);
        if (responders.isEmpty()) map.remove(clazz);
        log.debug("Removed: {} for {}", subscriber, clazz);
    }

    @Override
    public <E extends B, R> Response<R> emit(E payload) {
        try {
            return UnsafeCastUtil.cast(emitAsync(payload).get());
        } catch (InterruptedException | ExecutionException e) {
            log.catching(e);
            return new Response<R>().setComplete();
        }
    }

    @Override
    public <E extends B, R> CompletableFuture<Response<R>> emitAsync(E payload) {
        Class type = payload.getClass();
        if (!map.containsKey(type)) return CompletableFuture.completedFuture(new Response<R>().setComplete());
        Event<R> event = Event.withPayload(payload);
        return CompletableFuture.supplyAsync(() -> {
            log.debug("Emitting event: {}", payload);
            List<Consumer<Event<R>>> handlers = UnsafeCastUtil.cast(map.get(type));
            if (handlers.size() >= 50) {
                handlers.stream().parallel().forEach(consumer -> consumer.accept(event));
            } else {
                handlers.forEach(consumer -> consumer.accept(event));
            }
            log.debug("Processed event: {}", event);
            return event.getResponse().setComplete();
        }).exceptionally(throwable -> {
            log.catching(throwable.getCause());
            return event.getResponse().setComplete();
        });
    }

}
