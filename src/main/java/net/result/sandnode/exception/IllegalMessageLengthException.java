package net.result.sandnode.exception;

public class IllegalMessageLengthException extends SandnodeException {
    public IllegalMessageLengthException(int lengthInt) {
        super("Header length exceeds 65535: %d".formatted(lengthInt));
    }
}
