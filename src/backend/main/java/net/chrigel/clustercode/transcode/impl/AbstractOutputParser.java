package net.chrigel.clustercode.transcode.impl;

import lombok.Synchronized;
import net.chrigel.clustercode.process.OutputParser;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

public abstract class AbstractOutputParser implements OutputParser {

    private boolean started = false;
    private XLogger log = XLoggerFactory.getXLogger(getClass());

    @Synchronized
    @Override
    public final void start() {
        if (started) return;
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
        if (line == null || "".equals(line)) return;
        try {
            doParse(line);
        } catch (Exception ex) {
            log.catching(ex);
            log.warn("Shutting down parser.");
            stop();
        }
    }

    /**
     * Parses the given line.
     *
     * @param line the non-null line.
     */
    protected abstract void doParse(String line);

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
