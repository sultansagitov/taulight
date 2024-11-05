package net.result.openhelo;

import net.result.openhelo.exceptions.WrongTypeException;
import net.result.openhelo.messages.HeloMessage;
import net.result.openhelo.messages.OnlineResponseMessage;
import net.result.openhelo.messages.SandnodeMessageAdapter;
import net.result.openhelo.messages.TextMessage;
import net.result.sandnode.Hub;
import net.result.sandnode.config.HubConfig;
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
import java.util.ArrayList;
import java.util.List;

public class HeloHub extends Hub {
    private static final Logger LOGGER = LogManager.getLogger(HeloHub.class);

    public HeloHub(GlobalKeyStorage hubKeyStorage, @NotNull HubConfig hubConfig) {
        super(hubKeyStorage, hubConfig);
    }

    @Override
    public void onUserMessage(
            @NotNull RawMessage request,
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
                TextMessage echoTextMessage = (TextMessage) heloMessage;
                LOGGER.info("Data: {}", echoTextMessage.data);
                session.sendMessage(new SandnodeMessageAdapter(echoTextMessage));
            }
            case FORWARD -> {
                TextMessage forwardTextMessage = (TextMessage) heloMessage;
                LOGGER.info("Forwarding message: {}", forwardTextMessage.data);

                for (Session s : userSessionList) {
                    s.sendMessage(new SandnodeMessageAdapter(forwardTextMessage));
                    LOGGER.info("Message forwarded to session: {}", s.getIPString());
                }
            }
            case ONLINE -> {
                List<String> list = new ArrayList<>();
                for (Session session1 : userSessionList) {
                    String ipString = session1.getIPString();
                    list.add(ipString);
                }
                LOGGER.info("Online IPs: {}", list);
                OnlineResponseMessage onlineResponse = new OnlineResponseMessage(list);
                session.sendMessage(new SandnodeMessageAdapter(onlineResponse));
            }
        }

    }

}
