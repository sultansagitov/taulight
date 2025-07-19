package net.result.sandnode.exception;

public class MessageSerializationException extends SandnodeException {
    public MessageSerializationException(String message, Throwable e) {
        super(message, e);
    }

    public MessageSerializationException(Exception e) {
        super("%s: %s".formatted(e.getClass().getSimpleName(), e.getMessage()), e);
    }
}
