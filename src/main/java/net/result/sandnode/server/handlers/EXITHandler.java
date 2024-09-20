package net.result.sandnode.server.handlers;

import net.result.sandnode.messages.IMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EXITHandler implements IProtocolHandler {
    @Override
    public @Nullable IMessage getResponse(@NotNull IMessage request) {
        return null;
    }
}
