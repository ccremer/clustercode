package net.chrigel.clustercode.transcode.impl;

import net.chrigel.clustercode.transcode.OutputParser;

public abstract class AbstractOutputParser implements OutputParser {

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
