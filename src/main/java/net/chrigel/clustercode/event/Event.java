package net.chrigel.clustercode.event;

import lombok.Getter;
import lombok.ToString;

import java.util.Optional;
import java.util.UUID;

@ToString
public class Event<T> {

    @Getter
    private final T payload;
    private UUID messageID;

    public Event(T payload) {
        this.payload = payload;
    }

    public Event(T payload, UUID messageId) {
        this.payload = payload;
        this.messageID = messageId;
    }

    public Optional<UUID> getMessageId() {
        return Optional.ofNullable(messageID);
    }
}
