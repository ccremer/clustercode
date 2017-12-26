package net.chrigel.clustercode.event;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import lombok.Synchronized;
import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.util.UnsafeCastUtil;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

@XSlf4j
public class EventBusImpl<B extends Serializable> implements EventBus<B> {

    private final ListMultimap<Class, Consumer<Event<? extends B>>> map =
        MultimapBuilder.hashKeys().arrayListValues().build();

    @Synchronized
    @Override
    public <E extends B> void registerEventHandler(Class<E> clazz, Consumer<Event<E>> subscriber) {
        if (map.containsValue(subscriber)) {
            log.debug("Already registered: {}", subscriber);
            return;
        }
        map.put(clazz, UnsafeCastUtil.cast(subscriber));
        log.debug("Registered: {} for {}", subscriber, clazz.getName());
    }

    @Synchronized
    @Override
    public <E extends B> void unRegister(Class<E> clazz, Consumer<Event<E>> subscriber) {
        if (map.remove(clazz, subscriber)) {
            log.debug("Removed: {} for {}", subscriber, clazz);
        }
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
