package net.result.sandnode.exceptions;

import org.jetbrains.annotations.NotNull;

public class SocketAcceptionException extends SandnodeException {

    public SocketAcceptionException(String message, Throwable e) {
        super(message, e);
    }
}
