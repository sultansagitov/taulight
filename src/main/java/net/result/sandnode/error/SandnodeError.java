package net.result.sandnode.error;

import net.result.sandnode.message.types.ErrorMessage;

public interface SandnodeError {
    int getCode();

    String getDescription();

    ErrorMessage message();
}