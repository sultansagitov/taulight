package net.result.sandnode.exceptions;

public class ConfigurationException extends SandnodeRuntimeException {
    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(Exception e) {
        super(e);
    }
}
