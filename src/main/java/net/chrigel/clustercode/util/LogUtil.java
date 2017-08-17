package net.chrigel.clustercode.util;

import org.slf4j.ext.XLogger;

public class LogUtil {

    public static <T> T logWarnAndExit(T returnValue, XLogger logger, String message, Object... args) {
        logger.warn(message, args);
        return logger.exit(returnValue);
    }

}
