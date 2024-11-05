package net.result.sandnode.exceptions;

import net.result.sandnode.messages.util.Connection;
import org.jetbrains.annotations.NotNull;

public class WrongNodeUsed extends SandnodeException {
    public WrongNodeUsed(@NotNull Connection opposite) {
        super(opposite.name());
    }
}
