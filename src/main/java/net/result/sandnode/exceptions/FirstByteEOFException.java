package net.result.sandnode.exceptions;

import java.io.EOFException;

public class FirstByteEOFException extends EOFException {
    public FirstByteEOFException(String message) {
        super(message);
    }
}
