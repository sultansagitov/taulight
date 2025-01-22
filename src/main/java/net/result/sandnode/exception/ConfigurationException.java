package net.result.sandnode.exception;

public class ConfigurationException extends SandnodeException {
    public ConfigurationException(String message, Throwable e) {
        super(message, e);
    }

    public ConfigurationException(String message, String fileName) {
        super("%s: %s".formatted(message, fileName));
    }
}
