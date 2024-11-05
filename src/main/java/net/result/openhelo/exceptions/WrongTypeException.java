package net.result.openhelo.exceptions;

public class WrongTypeException extends Exception {
    public WrongTypeException(byte type) {
        super(String.format("Using byte %b", type));
    }
}
