package net.result.openhelo.exceptions;

public class WrongTypeException extends Exception {
    public WrongTypeException(byte type) {
        super("Using byte %b".formatted(type));
    }
}
