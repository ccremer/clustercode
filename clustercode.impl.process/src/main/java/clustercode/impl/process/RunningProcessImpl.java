package clustercode.impl.process;

import lombok.extern.slf4j.XSlf4j;
import clustercode.api.process.RunningExternalProcess;
import org.slf4j.ext.XLogger;

import java.util.concurrent.TimeUnit;

@XSlf4j
class RunningProcessImpl implements RunningExternalProcess {

    private Process process;

    RunningProcessImpl(Process process) {
        this.process = process;
    }

    @Override
    public RunningExternalProcess sleep(long millis) {
        return sleep(millis, TimeUnit.MILLISECONDS);
    }

    @Override
    public RunningExternalProcess sleep(long timeout, TimeUnit unit) {
        try {
            Thread.sleep(unit.toMillis(timeout));
        } catch (InterruptedException e) {
            log.catching(XLogger.Level.WARN, e);
        }
        return this;
    }

    @Override
    public void awaitDestruction() {
        try {
            log.debug("Waiting for process to destroy...");
            process.destroyForcibly().waitFor();
        } catch (InterruptedException e) {
            log.throwing(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean destroyNowWithTimeout(long timeout, TimeUnit unit) {
        try {
            log.warn("Waiting for process to destroy within {} {}...", timeout, unit.toString().toLowerCase());
            return process.destroyForcibly().waitFor(timeout, unit);
        } catch (InterruptedException e) {
            log.throwing(e);
            throw new RuntimeException(e);
        }
    }

}
