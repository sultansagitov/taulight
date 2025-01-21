package net.result.sandnode.server;

import net.result.sandnode.messages.types.ErrorMessage;

public interface SandnodeError {
    int getCode();

    String getDescription();

    ErrorMessage message();
}