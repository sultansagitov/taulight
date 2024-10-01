package net.result.sandnode.server.handlers;

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
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import static net.result.sandnode.messages.util.MessageType.TMPONLINE;

public class OnlineHandler implements IProtocolHandler {

    private static final OnlineHandler instance = new OnlineHandler();

    private OnlineHandler() {
    }

    public static OnlineHandler getInstance() {
        return instance;
    }

    @Override
    public @Nullable ICommand getCommand(
            @NotNull RawMessage request,
            @NotNull List<Session> sessionList,
            @NotNull Session session,
            @NotNull GlobalKeyStorage globalKeyStorage
    ) {
        HeadersBuilder headersBuilder = new HeadersBuilder()
                .set(request.getConnection().getOpposite())
                .set(TMPONLINE);
        JSONArray array = new JSONArray();
        for (Session s : sessionList) {
            JSONObject jsonSession = new JSONObject();
            jsonSession.put("itsme", s == session);
            jsonSession.put("uuid", s.uuid);
            jsonSession.put("address", "%s:%d".formatted(s.socket.getInetAddress().getHostAddress(), s.socket.getPort()));
            array.put(jsonSession);
        }

        JSONObject content = new JSONObject()
                .put("host", session.socket.getInetAddress().getHostName())
                .put("port", session.socket.getPort())
                .put("data", array);
        @NotNull IMessage response = new JSONMessage(headersBuilder, content);
        return new ResponseCommand(response);
    }
}
