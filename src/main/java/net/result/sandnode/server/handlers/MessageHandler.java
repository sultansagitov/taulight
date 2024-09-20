package net.result.sandnode.server.handlers;

import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.messages.HeadersBuilder;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.JSONMessage;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static net.result.sandnode.messages.util.MessageType.MESSAGE;

public final class MessageHandler implements IProtocolHandler {

    public MessageHandler() {
    }

    @Override
    public @NotNull IMessage getResponse(@NotNull IMessage request) throws IOException, ReadingKeyException, NoSuchAlgorithmException, DecryptionException {
        final JSONObject content;
        if (request instanceof JSONMessage jsonMessage) content = jsonMessage.getContent();
        else content = new JSONObject().put("error", "error");

        JSONObject jsonObject = new JSONObject()
                .put("headers", request.getHeaders())
                .put("request", content);

        HeadersBuilder headersBuilder = new HeadersBuilder()
                .set(request.getHeaders().getConnection().getOpposite())
                .set(MESSAGE);

        return new JSONMessage(headersBuilder, jsonObject);
    }
}
