package clustercode.api.process;

import java.util.concurrent.TimeUnit;

/**
 * Represents and wraps a started external process.
 */
public interface RunningExternalProcess {

    /**
     * Causes the current thread to wait for the given time.
     *
     * @param millis the timeout in milliseconds, {@literal > 0}.
     * @return this.
     * @throws IllegalArgumentException if timeout is negative.
     */
    RunningExternalProcess sleep(long millis);

    /**
     * Causes the current thread to wait for the given time.
     *
     * @param timeout the timeout, {@literal > 0}.
     * @param unit    the time unit for the timeout parameter, not null.
     * @return this.
     * @throws IllegalArgumentException if timeout is negative.
     */
    RunningExternalProcess sleep(long timeout, TimeUnit unit);

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
