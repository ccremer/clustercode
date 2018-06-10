package clustercode.test.util;

import org.assertj.core.api.Assertions;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

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
            Assertions.fail(e.getMessage());
        }
    }

}
