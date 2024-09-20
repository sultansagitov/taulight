package net.result.sandnode.exceptions;

public class NoSuchEncoderException extends SandnodeException {
    public NoSuchEncoderException(String message) {
        super(message);
    }

    public NoSuchEncoderException(Exception e) {
        super(e);
    }
}
