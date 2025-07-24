package net.result.sandnode.exception;

public class DeserializationException extends ProtocolException {
    public DeserializationException(String message) {
        super(message);
    }

    public DeserializationException(Throwable e) {
        super(e);
    }
}
