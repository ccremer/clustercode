package clustercode.impl.util;

import org.slf4j.helpers.MessageFormatter;

public class InvalidConfigurationException extends RuntimeException {

    public InvalidConfigurationException() {
        super();
    }

    public InvalidConfigurationException(String message) {
        super(message);
    }

    public InvalidConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidConfigurationException(String formatString, Object... args) {
        super(MessageFormatter.arrayFormat(formatString, args).getMessage());
    }

    public InvalidConfigurationException(String formatString, Throwable cause, Object... args) {
        super(MessageFormatter.arrayFormat(formatString, args).getMessage(), cause);
    }

    public InvalidConfigurationException(Throwable cause) {
        super(cause);
    }

}
