package clustercode.test.integration;

import org.rnorth.ducttape.TimeoutException;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class RunOnceWaitStrategy extends AbstractWaitStrategy {

    public RunOnceWaitStrategy() {
        this.withStartupTimeout(Duration.ofHours(1));
    }

    @Override
    protected void waitUntilReady() {

        try {
            Unreliables.retryUntilTrue((int) startupTimeout.getSeconds(), TimeUnit.SECONDS,
                    () -> getRateLimiter().getWhenReady(() -> waitStrategyTarget.isRunning()));

        } catch (TimeoutException ex) {
            throw new ContainerLaunchException("Timed out waiting for container to be created.");
        }

        try {
            Unreliables.retryUntilTrue((int) startupTimeout.getSeconds(), TimeUnit.SECONDS,
                    () -> getRateLimiter().getWhenReady(() -> !waitStrategyTarget.isRunning() && waitStrategyTarget.getCurrentContainerInfo().getState().getExitCode() == 0));
        } catch (TimeoutException ex) {
            throw new ContainerLaunchException("Timed out waiting for container to be created.");
        }

    }
}
