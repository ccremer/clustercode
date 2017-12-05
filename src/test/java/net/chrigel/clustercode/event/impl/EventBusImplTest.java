package net.chrigel.clustercode.event.impl;

import net.chrigel.clustercode.event.Event;
import net.chrigel.clustercode.util.OptionalFunction;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class EventBusImplTest {

    private EventBusImpl<Message> subject;

    @Before
    public void setUp() throws Exception {
        subject = new EventBusImpl<>();
    }

    @Test
    public void register_ShouldIgnoreSameListener() {
        AtomicInteger counter = new AtomicInteger();
        OptionalFunction<Event<Message>, Object> responder = OptionalFunction.ofNullable(
            event -> counter.incrementAndGet());

        subject.registerEventHandler(Message.class, responder);
        subject.registerEventHandler(Message.class, responder);

        subject.emit(new Event<>(new Message(1)));

        assertThat(counter).hasValue(1);
    }

    @Test
    public void unRegister_ShouldRemoveListener() {
        AtomicInteger counter = new AtomicInteger();
        OptionalFunction<Event<Message>, Object> responder = OptionalFunction.ofNullable(
            event -> counter.incrementAndGet());

        subject.registerEventHandler(Message.class, responder);
        subject.unRegister(Message.class, responder);

        subject.emit(new Event<>(new Message(1)));

        assertThat(counter).hasValue(0);
    }

    @Test
    public void emit_ShouldPassEventToListener() throws Exception {
        AtomicBoolean received = new AtomicBoolean();
        subject.registerEventHandler(Message.class, OptionalFunction.empty(event -> received.set(true)));
        subject.emit(new Event<>(new Message()));

        assertThat(received).isTrue();
    }

    @Test(timeout = 1000)
    public void emitAsync_ShouldPassEventToListener() throws Exception {
        AtomicBoolean received = new AtomicBoolean();
        Thread testThread = Thread.currentThread();
        subject.registerEventHandler(Message.class, OptionalFunction.empty(event -> {
            assertThat(testThread).isNotEqualTo(Thread.currentThread());
            received.set(true);
        }));
        CompletableFuture future = subject.emitAsync(new Event<>(new Message()));

        future.get();
        assertThat(received).isTrue();
    }

    @Test(timeout = 1000)
    public void emitAsync_ShouldDoNothing_IfNoListener() throws Exception {
        CompletableFuture result = subject.emitAsync(new Event<>(new Message()));

        result.get();
    }

    @Test
    public void emitAndGet_ShouldReturnResultFromResponder() throws Exception {
        subject.registerEventHandler(Message.class,
            OptionalFunction.ofNullable(event -> event.getPayload().getNumber()));
        Optional<Integer> result = subject.emitAndGet(new Event<>(new Message(5)));

        assertThat(result).contains(5);
    }

    @Test
    public void emitAndGetAll_ShouldReturnResultFromResponder() throws Exception {
        subject.registerEventHandler(Message.class,
            OptionalFunction.ofNullable(event -> 7));
        Collection<Integer> result = subject.emitAndGetAll(new Event<>(new Message(5)));

        assertThat(result).contains(7);
    }

    @Test
    public void emitAndGetAll_ShouldReturnResultFromSecondResponder_IfFirstResponderDoesNotReturnValue() {
        subject.registerEventHandler(Message.class, OptionalFunction.empty(event -> {/* Do Nothing */}));
        subject.registerEventHandler(Message.class,
            OptionalFunction.ofNullable(event -> event.getPayload().getNumber()));
        Collection<Integer> result = subject.emitAndGetAll(new Event<>(new Message(5)));

        assertThat(result).containsExactly(5);
    }

    @Test
    public void emitAndGetAll_ShouldReturnResultFromAllResponder_IfFirstResponderDoesNotReturnValue() {
        subject.registerEventHandler(Message.class, OptionalFunction.empty(event -> {/* Do Nothing */}));
        subject.registerEventHandler(Message.class,
            OptionalFunction.ofNullable(event -> 1));
        subject.registerEventHandler(Message.class,
            OptionalFunction.ofNullable(event -> 2));
        Collection<Integer> result = subject.emitAndGetAll(new Event<>(new Message(5)));

        assertThat(result).containsExactly(1, 2);
    }

    private static class Message implements Serializable {

        private Integer payload;

        Message() {
        }

        Message(Integer payload) {
            this.payload = payload;
        }

        Integer getNumber() {
            return payload;
        }
    }
}
