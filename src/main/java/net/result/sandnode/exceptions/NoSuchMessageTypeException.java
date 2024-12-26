package net.result.sandnode.exceptions;

public class NoSuchMessageTypeException extends SandnodeException {
    public NoSuchMessageTypeException(byte b) {
        super("Can't find MessageType for %8s".formatted(Integer.toBinaryString(b & 0xFF).replace(' ', '0')));
    }
}
