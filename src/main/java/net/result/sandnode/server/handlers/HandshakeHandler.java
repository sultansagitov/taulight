package net.result.sandnode.server.handlers;

import net.result.sandnode.messages.HeadersBuilder;
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

import static net.result.sandnode.messages.util.MessageType.HANDSHAKE;
import static net.result.sandnode.util.encryption.Encryption.AES;

public class HandshakeHandler implements IProtocolHandler {

    @Override
    public @Nullable ICommand getCommand(
            @NotNull RawMessage request,
            @NotNull List<Session> sessionList,
            @NotNull Session session,
            @NotNull GlobalKeyStorage globalKeyStorage
    ) {
        HeadersBuilder headersBuilder = new HeadersBuilder()
                .set(request.getConnection().getOpposite())
                .set(HANDSHAKE)
                .set(AES);

        JSONObject jsonObject = new JSONObject().put("session-id", session.uuid.toString());
        JSONMessage response = new JSONMessage(headersBuilder, jsonObject);

        return new ResponseCommand(response);
    }

}
