package net.result.openhelo;

import net.result.openhelo.exceptions.WrongTypeException;
import net.result.openhelo.messages.HeloMessage;
import net.result.openhelo.messages.OnlineResponseMessage;
import net.result.openhelo.messages.SandnodeMessageAdapter;
import net.result.openhelo.messages.TextMessage;
import net.result.sandnode.Hub;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.server.Session;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

public class HeloHub extends Hub {
    private static final Logger LOGGER = LogManager.getLogger(HeloHub.class);

    public HeloHub(GlobalKeyStorage hubKeyStorage) {
        super(hubKeyStorage);
    }

    @Override
    public void onUserMessage(
            @NotNull RawMessage request,
            @NotNull List<Session> sessionList,
            @NotNull Session session
    ) throws ReadingKeyException, EncryptionException, IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(request.getBody());
        HeloType heloType;

        try {
            heloType = HeloType.fromByte((byte) in.read());
        } catch (WrongTypeException e) {
            throw new RuntimeException(e);
        }

        HeloMessage heloMessage = heloType.fromBytes(in.readAllBytes());

        switch (heloType) {
            case ECHO -> {
                TextMessage textMessage = (TextMessage) heloMessage;
                LOGGER.info("Data: {}", textMessage.data);
                session.sendMessage(new SandnodeMessageAdapter(textMessage));
            }
            case FORWARD -> {
                TextMessage textMessage = (TextMessage) heloMessage;
                LOGGER.info("Forwarding message: {}", textMessage.data);

                for (Session s : userSessionList) {
                    s.sendMessage(new SandnodeMessageAdapter(textMessage));
                    LOGGER.info("Message forwarded to session: {}", s.getIPString());
                }
            }
            case ONLINE -> {
                List<String> list = userSessionList.stream().map(Session::getIPString).toList();
                LOGGER.info("Online IPs: {}", list);
                OnlineResponseMessage onlineResponse = new OnlineResponseMessage(list);
                session.sendMessage(new SandnodeMessageAdapter(onlineResponse));
            }
        }

    }

}
