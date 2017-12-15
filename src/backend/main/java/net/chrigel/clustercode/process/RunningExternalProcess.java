package net.chrigel.clustercode.process;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Represents and wraps a started external process. Provides a fluent-like API and throws runtime exceptions on
 * unexpected or configuration errors. Generally passing null is regarded as programmer error. The implementation
 * is not guaranteed to be thread-safe. For each subprocess to be started use separate instances. A subprocess can
 * only be launched once. This interface is not meant for dependency injection.
 */
public interface RunningExternalProcess {

    /**
     * Causes the current thread to wait for the given time.
     *
     * @param millis the timeout in milliseconds, {@literal > 0}.
     * @return this.
     */
    RunningExternalProcess sleep(long millis);

    /**
     * Causes the current thread to wait for the given time.
     *
     * @param timeout the timeout, {@literal > 0}.
     * @param unit    the time unit for the timeout parameter, not null.
     * @return this.
     */
    RunningExternalProcess sleep(long timeout, TimeUnit unit);

    /**
     * Waits for the process to terminate and returns the exit code, if present.
     *
     * @return an optional with the exit code. If there was an exception at running the process, the result will be
     * empty and logged as error.
     */
    Optional<Integer> waitFor();

    /**
     * Waits for the specified amount of time before the process will be terminated forcefully. No checks will be
     * made if the process could be or is already terminated. Use {@link #destroyNowWithTimeout(long, TimeUnit)} or
     * {@link #awaitDestruction()} with  method chaining to ensure the subprocess terminates. This method itself
     * returns immediately after invoking. The time elapses immediately after invoking this method.
     *
     * @param millis the waiting time in milliseconds, {@literal > 0}.
     */
    void destroyAfter(long millis);

    /**
     * Waits for the specified amount of time before the process will be terminated forcefully. No checks will be
     * made if the process could be or is already terminated. Use {@link #destroyNowWithTimeout(long, TimeUnit)} or
     * {@link #awaitDestruction()} with  method chaining to ensure the subprocess terminates. This method itself
     * returns immediately after invoking. The time elapses immediately after invoking this method.
     *
     * @param timeout the waiting time, {@literal > 0}.
     * @param unit    the time unit, not null.
     */
    void destroyAfter(long timeout, TimeUnit unit);

    /**
     * Waits (indefinitly) for the termination of the subprocess. This method returns immediately if no process is
     * running.
     */
    void awaitDestruction();

    /**
     * Terminates the process with a timeout. This method returns earlier if the process terminated before the
     * timeout occurred.
     *
     * @param timeout the time to wait for termination ({@literal > 0}).
     * @param unit    the unit of {@code timeout}, not null.
     * @return true if the process terminated within the given timeout. False if the timeout occurred and the process
     * may still be running. A warning will be logged if that happens. Also returns true if no process is active.
     */
    boolean destroyNowWithTimeout(long timeout, TimeUnit unit);

}
