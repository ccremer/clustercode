package net.chrigel.clustercode.event;

import lombok.Getter;

import java.util.EventObject;

@Getter
public class Event<T> extends EventObject {

    private final T payload;

    public Event(Object source, T payload) {
        super(source);
        this.payload = payload;
    }

}
