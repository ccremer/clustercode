package net.chrigel.clustercode.util;

import lombok.Synchronized;

import java.util.Locale;

public enum Platform {

    WINDOWS,
    MAC,
    LINUX,
    OTHER;

    private static Platform detectedOS;

    @Synchronized
    public static Platform currentPlatform() {
        if (detectedOS == null) {
            String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
            if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
                detectedOS = Platform.MAC;
            } else if (OS.indexOf("win") >= 0) {
                detectedOS = Platform.WINDOWS;
            } else if (OS.indexOf("nux") >= 0) {
                detectedOS = Platform.LINUX;
            } else {
                detectedOS = Platform.OTHER;
            }
        }
        return detectedOS;
    }

}
