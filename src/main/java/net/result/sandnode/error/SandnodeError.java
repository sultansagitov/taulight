package net.result.sandnode.error;

import net.result.sandnode.exception.error.SandnodeErrorException;
import org.jetbrains.annotations.Contract;

public interface SandnodeError {
    String code();

    String description();

    @Contract(" -> new")
    SandnodeErrorException exception();

}