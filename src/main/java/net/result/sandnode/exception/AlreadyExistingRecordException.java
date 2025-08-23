package net.result.sandnode.exception;

public class AlreadyExistingRecordException extends SandnodeException {
    public AlreadyExistingRecordException(String type, String field, Object value) {
        super("%s with %s %s already exists.".formatted(type, field, value));
    }
}
