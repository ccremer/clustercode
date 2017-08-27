package net.chrigel.clustercode.util;

import lombok.Synchronized;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractWeakPublisher<T>
        implements Publisher<T> {


    private List<Consumer<T>> subscribers = new LinkedList<>();

    @Synchronized
    @Override
    public void register(Consumer<T> subscriber) {
        if (subscribers.contains(subscriber)) return;
        subscribers.add(subscriber);
    }

    @Synchronized
    @Override
    public void unRegister(Consumer<T> subscriber) {
        this.subscribers.remove(subscriber);
    }

    @Synchronized
    protected void publishPayload(T payload) {
        this.subscribers.forEach(subscriber -> subscriber.accept(payload));
    }

}
