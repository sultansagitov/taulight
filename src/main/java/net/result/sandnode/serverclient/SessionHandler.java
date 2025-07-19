package net.result.sandnode.serverclient;

import net.result.sandnode.exception.SandnodeException;
import net.result.sandnode.message.EncryptedMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Connection;
import net.result.sandnode.util.MessageUtil;
import net.result.sandnode.util.StreamReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.net.Socket;

public class SessionHandler {
    private static final Logger LOGGER = LogManager.getLogger(SessionHandler.class);

    static void handle(SandnodeServer server, String ip, Socket clientSocket) {
        Thread.currentThread().setName(ip);

        try {
            InputStream inputStream = StreamReader.inputStream(clientSocket);
            EncryptedMessage encrypted = EncryptedMessage.readMessage(inputStream);
            RawMessage request = MessageUtil.decryptMessage(encrypted, server.node.keyStorageRegistry);
            Connection conn = request.headers().connection();
            Session session = server.createSession(clientSocket, conn.getOpposite());
            session.io.chainManager.distributeMessage(request);
            session.start();
        } catch (SandnodeException | InterruptedException e) {
            LOGGER.error("Error handling session for client {}: {}", ip, e.getMessage(), e);
        }
    }
}
