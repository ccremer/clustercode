package net.chrigel.clustercode.transcode.impl;

import lombok.Synchronized;
import lombok.val;
import net.chrigel.clustercode.process.OutputParser;
import net.chrigel.clustercode.util.AbstractPublisher;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.util.Optional;

public abstract class AbstractOutputParser<T> extends AbstractPublisher<T> implements OutputParser<T> {

    private boolean started = false;
    private T result;
    private XLogger log = XLoggerFactory.getXLogger(getClass());

    @Synchronized
    @Override
    public final void start() {
        if (started) return;
        this.result = null;
        doStart();
        this.started = true;
    }

    @Synchronized
    @Override
    public final void stop() {
        doStop();
        this.started = false;
    }

    @Override
    public final boolean isStarted() {
        return started;
    }

    @Synchronized
    @Override
    public final void parse(String line) {
      //  if (!isStarted()) return;
        if (line == null || "".equals(line)) return;
        try {
            val result = doParse(line);
            if (result == null) return;
            this.result = result;
            publishPayload(result);
        } catch (Exception ex) {
            log.catching(ex);
            log.warn("Shutting down parser.");
            stop();
        }
    }

    @Override
    public final Optional<T> getResult() {
        return Optional.ofNullable(result);
    }

    /**
     * Parses the given line.
     *
     * @param line the non-null line.
     * @return the result, otherwise null.
     */
    protected abstract T doParse(String line);

    protected abstract void doStart();

    protected abstract void doStop();

    protected final double getDoubleOrDefault(String value, double defaultValue) {
        if (value == null) return defaultValue;
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    protected final long getLongOrDefault(String value, long defaultValue) {
        if (value == null) return defaultValue;
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

}
