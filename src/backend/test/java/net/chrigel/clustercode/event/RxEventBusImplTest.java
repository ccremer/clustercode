package net.chrigel.clustercode.event;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class RxEventBusImplTest {

    private RxEventBusImpl subject;

    @Before
    public void setUp() throws Exception {
        subject = new RxEventBusImpl();
    }

    @Test
    public void shouldNotifySubscriber() {
        String message = "hello";
        AtomicBoolean called = new AtomicBoolean();

        subject.register(String.class, value -> {
            assertThat(value).isEqualTo(message);
            called.set(true);
        });
        subject.emit(message);

        assertThat(called).isTrue();
    }

    @Test
    public void shouldThrowException_FromSubscriber() {
        String message = "hello";

        subject.register(String.class, value -> {
            throw new RuntimeException(message);
        }, ex -> assertThat(ex).hasMessage(message));
        subject.emit(message);

    }

    @Test
    public void register_ShouldFilter_AndNotifySubscriber() {
        String message = "hello";
        Object ignore = new Object();
        AtomicInteger called = new AtomicInteger();

        subject.register(String.class, value -> {
            assertThat(value).isEqualTo(message);
            called.incrementAndGet();
        });

        subject.emit(message);
        subject.emit(ignore);

        assertThat(called).hasValue(1);
    }

    @Test
    public void register_ShouldIgnoreMessage_FromSubscribers_ThatAreRemoved() {
        String message = "hello";
        AtomicInteger called = new AtomicInteger();

        subject.register(String.class, value -> fail("This should not be called."))
               .dispose();

        subject.emit(message);

        assertThat(called).hasValue(0);
    }

    @Test
    public void emit_ShouldEmitSynchronously() {
        Message message = new Message();

        subject.register(Message.class, Message::increment);
        subject.register(Message.class, Message::increment);
        subject.emit(message);

        assertThat(message.getValue()).isEqualTo(2);
    }

    @Test(timeout = 1000)
    public void emitAsync_ShouldEmitAsynchronously() throws Exception {
        Message message = new Message();

        subject.register(Message.class, Message::increment);
        subject.register(Message.class, Message::increment);
        CompletableFuture<Message> result = subject.emitAsync(message);

        assertThat(result.get().getValue()).isEqualTo(2);
    }

    private static class Message {

        private int value;

        void increment() {
            this.value += 1;
        }

        int getValue() {
            return value;
        }
    }

}
