package net.result.sandnode.exceptions;

public class ConfigurationException extends SandnodeException {
    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable e) {
        super(message, e);
    }

    public ConfigurationException(String message, String fileName) {
        super(String.format("%s: %s", message, fileName));
    }
}
