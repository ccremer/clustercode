package clustercode.impl.util;

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
            if ((OS.contains("mac")) || (OS.contains("darwin"))) {
                detectedOS = Platform.MAC;
            } else if (OS.contains("win")) {
                detectedOS = Platform.WINDOWS;
            } else if (OS.contains("nux")) {
                detectedOS = Platform.LINUX;
            } else {
                detectedOS = Platform.OTHER;
            }
        }
        return detectedOS;
    }

}
