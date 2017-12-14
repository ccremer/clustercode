package net.chrigel.clustercode.event.impl;

import net.chrigel.clustercode.event.Event;
import net.chrigel.clustercode.event.EventBusImpl;
import net.chrigel.clustercode.event.Response;
import net.chrigel.clustercode.util.OptionalFunction;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class EventBusImplTest {

    private EventBusImpl<Message> subject;

    @Before
    public void setUp() throws Exception {
        subject = new EventBusImpl<>();
    }

    @Test
    public void register_ShouldIgnoreSameListener() {
        Consumer<Event<Message>> responder = event -> event.addAnswer(1);
        subject.registerEventHandler(Message.class, responder);
        subject.registerEventHandler(Message.class, responder);
        Response<Integer> response = subject.emit(new Message(1));

        assertThat(response.getAnswers()).hasSize(1);
    }

    @Test
    public void unRegister_ShouldRemoveListener() {
        Consumer<Event<Message>> responder = event -> event.addAnswer(1);

        subject.registerEventHandler(Message.class, responder);
        subject.unRegister(Message.class, responder);

        Response<Integer> response = subject.emit(new Message(1));

        assertThat(response.getAnswer()).isEmpty();
    }

    @Test(timeout = 1000)
    public void emitAsync_ShouldReturnResponse_WithAnswer() throws Exception {
        Thread testThread = Thread.currentThread();
        subject.registerEventHandler(Message.class, event -> {
            assertThat(testThread).isNotEqualTo(Thread.currentThread());
            event.addAnswer(true);
        });
        CompletableFuture<Response<Message>> future = subject.emitAsync(new Message());

        assertThat(future.get().getAnswer(Boolean.class)).contains(true);
    }

    @Test(timeout = 1000)
    public void emitAsync_ShouldDoNothing_IfNoListener() throws Exception {
        CompletableFuture<Response<Message>> result = subject.emitAsync(new Message());

        assertThat(result.isDone()).isTrue();
        assertThat(result.get().getAnswer()).isEmpty();
    }

    @Test
    public void emitAsync_ShouldCatchException() throws Exception {
        subject.registerEventHandler(Message.class, event -> event.addAnswer(5));
        subject.registerEventHandler(Message.class, event -> {throw new RuntimeException();});
        CompletableFuture<Response<Integer>> result = subject.emitAsync(new Message(5));

        assertThat(result.get().getAnswer()).contains(5);
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
