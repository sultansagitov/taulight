package net.result.sandnode.error;

import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.types.ErrorMessage;
import org.jetbrains.annotations.Contract;

public interface SandnodeError {
    int code();

    String description();

    @Contract(" -> new")
    SandnodeErrorException exception();

    @Contract(" -> new")
    ErrorMessage createMessage();
}