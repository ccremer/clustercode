package net.chrigel.clustercode.event;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import net.chrigel.clustercode.util.UnsafeCastUtil;

@ToString
public class Event<T> {

    @Getter
    private final T payload;
    @Getter(AccessLevel.PACKAGE)
    private final Response<T> response = new Response<>();

    private Event(T payload) {
        this.payload = payload;
    }

    static <R> Event<R> withPayload(Object payload) {
        return new Event<>(UnsafeCastUtil.cast(payload));
    }

    /**
     * Adds an answer to this event. The answer will be ignored if it is null or the event is considered complete. This
     * method is best called in the event handler method and may be called multiple times (even per subscriber).
     *
     * @param answer the return value.
     */
    public void addAnswer(Object answer) {
        response.addAnswer(answer);
    }

}
