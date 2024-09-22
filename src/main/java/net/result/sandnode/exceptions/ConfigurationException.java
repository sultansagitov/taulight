package net.result.sandnode.exceptions;

public class ConfigurationException extends RuntimeException {
    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(Exception e) {
        super(e);
    }
}
