package net.result.sandnode.exceptions;

import net.result.sandnode.messages.util.Connection;

public class WrongNodeUsedException extends SandnodeException {
    public WrongNodeUsedException(Connection opposite) {
        super(opposite.name());
    }
}
