package net.chrigel.clustercode.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public interface CompletableUnitTest {

    AtomicReference<CountDownLatch> _latch = new AtomicReference<>(new CountDownLatch(1));

    default void completeOne() {
        _latch.get().countDown();
    }

    default void setExpectedCountForCompletion(int count) {
        _latch.set(new CountDownLatch(count));
    }

    default void waitForCompletion() {
        try {
            _latch.get().await();
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }

}
