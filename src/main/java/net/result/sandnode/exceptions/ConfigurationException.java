package net.result.sandnode.exceptions;

public class ConfigurationException extends SandnodeException {
    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Exception e) {
        super(message, e);
    }
}
