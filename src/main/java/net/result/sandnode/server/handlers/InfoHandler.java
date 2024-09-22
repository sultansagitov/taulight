package net.result.sandnode.server.handlers;

import net.result.sandnode.config.ServerConfigSingleton;
import net.result.sandnode.messages.HeadersBuilder;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.JSONMessage;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.server.Session;
import net.result.sandnode.server.commands.ICommand;
import net.result.sandnode.server.commands.ResponseCommand;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.List;

public class InfoHandler implements IProtocolHandler {

    @Override
    public @Nullable ICommand getCommand(
            @NotNull RawMessage request,
            @NotNull List<Session> sessionList,
            @NotNull Session session,
            @NotNull GlobalKeyStorage globalKeyStorage) {
        HeadersBuilder headersBuilder = new HeadersBuilder()
                .set(request.getConnection().getOpposite())
                .set(ServerConfigSingleton.getInfoMIME());
        @NotNull JSONObject content = new JSONObject().put("data", ServerConfigSingleton.getInfo());
        IMessage response = new JSONMessage(headersBuilder, content);
        return new ResponseCommand(response);
    }

}
