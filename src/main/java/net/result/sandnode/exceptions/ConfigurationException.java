package net.result.sandnode.exceptions;

public class ConfigurationException extends RuntimeException {
    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(Exception e) {
        super(e);
    }

    public ConfigurationException(String message, Exception e) {
        super(message, e);
    }
}
