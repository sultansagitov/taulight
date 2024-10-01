package net.result.sandnode;

import net.result.sandnode.exceptions.NoSuchReqHandler;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.server.Session;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static net.result.sandnode.util.encryption.Encryption.AES;

public class ServerWithKeysTest {
    private static final Logger LOGGER = LogManager.getLogger(ServerWithKeysTest.class);

    @Test
    public void test() throws IOException, ReadingKeyException, EncryptionException, InterruptedException, NoSuchEncryptionException, NoSuchAlgorithmException, DecryptionException, NoSuchReqHandler {
        int port = ServerTest.getPort();

        GlobalKeyStorage serverKeyStorage = ServerTest.getServerKeyStorage();

        ServerTest.ServerThread serverThread = new ServerTest.ServerThread(serverKeyStorage, port);
        serverThread.start();

        Thread.sleep(1000);

        ServerTest.ClientThread clientThread = new ServerTest.ClientThread(port, true);
        clientThread.start();

        Thread.sleep(1000);



        Session session = serverThread.server.sessionList.get(0);

        RawMessage node1Message = ServerTest.getMessage(AES);

        // Sending server to client
        session.sendMessage(node1Message);
        LOGGER.info("Message sent");

        // Receiving client from server
        IMessage node2Message = clientThread.client.receiveMessage();
        LOGGER.info("Message received");

        ServerTest.messagesTest(node1Message, node2Message);



        clientThread.client.close();
        LOGGER.info("Client closed");
        serverThread.server.close();
        LOGGER.info("Server closed");
    }

}
