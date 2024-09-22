package net.result.sandnode.server.handlers;

import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.server.Session;
import net.result.sandnode.server.commands.ICommand;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IProtocolHandler {

    @Nullable ICommand getCommand(
            @NotNull RawMessage request,
            @NotNull List<Session> sessionList,
            @NotNull Session session,
            @NotNull GlobalKeyStorage globalKeyStorage);

}
