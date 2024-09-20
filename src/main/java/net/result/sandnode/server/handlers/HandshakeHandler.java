package net.result.sandnode.server.handlers;

import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.messages.HeadersBuilder;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.JSONMessage;
import net.result.sandnode.server.Session;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.aes.AESKeyDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.List;

import static net.result.sandnode.messages.util.MessageType.HANDSHAKE;
import static net.result.sandnode.util.encryption.Encryption.AES;

public class HandshakeHandler implements IProtocolHandler {
    private static final Logger LOGGER = LogManager.getLogger(HandshakeHandler.class);
    private final List<Session> sessionList;
    public final GlobalKeyStorage globalKeyStorage;

    public HandshakeHandler(
            final @NotNull List<Session> sessionList,
            final @NotNull GlobalKeyStorage globalKeyStorage) {
        this.sessionList = sessionList;
        this.globalKeyStorage = globalKeyStorage;
    }

    @Override
    public IMessage getResponse(@NotNull IMessage request) throws ReadingKeyException, EncryptionException {
        Session session = new Session();
        session.setKey(AESKeyDecoder.getKeyStore(request.getBody()));
        session.setUserAgent(request.getHeaders().get("User-agent"));
        sessionList.add(session);

        HeadersBuilder headersBuilder = new HeadersBuilder()
                .set(request.getHeaders().getConnection().getOpposite())
                .set(HANDSHAKE)
                .set("application/json")
                .set(AES);

        JSONObject jsonObject = new JSONObject()
                .put("session-id", session.uuid.toString());

        return new JSONMessage(headersBuilder, jsonObject);
    }

}
