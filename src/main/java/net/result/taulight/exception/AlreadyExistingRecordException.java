package net.result.taulight.exception;

import net.result.sandnode.exception.SandnodeException;

public class AlreadyExistingRecordException extends SandnodeException {
    public AlreadyExistingRecordException(String type, String field, Object value, Throwable e) {
        super("%s with %s %s already exists.".formatted(type, field, value), e);
    }
}
