package net.result.sandnode.exception;

import net.result.sandnode.message.util.Connection;

public class WrongNodeUsedException extends SandnodeException {
    public WrongNodeUsedException(Connection opposite) {
        super(opposite.name());
    }
}
