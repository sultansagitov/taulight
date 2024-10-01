package net.result.sandnode.server.handlers;

import net.result.sandnode.messages.HeadersBuilder;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.JSONMessage;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.server.Session;
import net.result.sandnode.server.commands.ICommand;
import net.result.sandnode.server.commands.ResponseCommand;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.List;

import static net.result.sandnode.messages.util.MessageType.MESSAGE;

public final class MessageHandler implements IProtocolHandler {

    private static final Logger LOGGER = LogManager.getLogger(MessageHandler.class);
    private static final MessageHandler instance = new MessageHandler();

    private MessageHandler() {
    }

    public static MessageHandler getInstance() {
        return instance;
    }

    @Override
    public @NotNull ICommand getCommand(
            @NotNull RawMessage request,
            @NotNull List<Session> sessionList,
            @NotNull Session session,
            @NotNull GlobalKeyStorage globalKeyStorage
    ) {
        JSONMessage jsonRequest = new JSONMessage(request);
        JSONObject requestContent = jsonRequest.getContent();

        String data = jsonRequest.getContent().getString("data");
        LOGGER.info("Data: {}", data);

        IMessage response;
        JSONObject responseContent;

        HeadersBuilder headersBuilder = new HeadersBuilder()
                .set(request.getConnection().getOpposite())
                .set(request.getEncryption())
                .set(MESSAGE);

        responseContent = new JSONObject()
                .put("host", session.socket.getInetAddress().getHostName())
                .put("port", session.socket.getPort())
                .put("headers", request.getHeaders())
                .put("request", requestContent);

        response = new JSONMessage(headersBuilder, responseContent);

        return new ResponseCommand(response);
    }
}
