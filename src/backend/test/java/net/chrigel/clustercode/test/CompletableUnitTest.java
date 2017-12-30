package net.chrigel.clustercode.test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

public interface CompletableUnitTest {

    AtomicReference<CompletableFuture<Integer>> _countFuture = new AtomicReference<>();
    AtomicInteger _count = new AtomicInteger();

    default void setupCompletable() {
        _count.set(0);
        _countFuture.set(new CompletableFuture<>());
    }

    default void incrementCounter() {
        _count.incrementAndGet();
    }

    default void incrementAndComplete() {
        _countFuture.get().complete(_count.incrementAndGet());
    }

    default void waitForCompletion() {
        waitForCompletion(1);
    }

    default void waitForCompletion(int count) {
        assertThat(_countFuture.get().join()).isEqualTo(count);
        assertThat(_countFuture.get()).isCompleted();
    }

}
