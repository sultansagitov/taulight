package net.result.openhelo;

import net.result.openhelo.messages.HeloMessageTypes;
import net.result.openhelo.messages.OnlineResponseMessage;
import net.result.openhelo.messages.TextMessage;
import net.result.sandnode.Hub;
import net.result.sandnode.config.IHubConfig;
import net.result.sandnode.exceptions.KeyStorageNotFoundException;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.server.Session;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HeloHub extends Hub {
    private static final Logger LOGGER = LogManager.getLogger(HeloHub.class);

    public HeloHub(GlobalKeyStorage hubKeyStorage, @NotNull IHubConfig hubConfig) {
        super(hubKeyStorage, hubConfig);
    }

    @Override
    public void onUserMessage(
            @NotNull IMessage request,
            @NotNull Session session
    ) throws ReadingKeyException, EncryptionException, IOException, KeyStorageNotFoundException {
        switch ((HeloMessageTypes) request.getHeaders().getType()) {
            case ECH -> {
                TextMessage echoTextMessage = new TextMessage(request);
                LOGGER.info("Data: {}", echoTextMessage.data);
                session.sendMessage(echoTextMessage);
            }
            case FWD -> {
                TextMessage forwardTextMessage = new TextMessage(request);
                LOGGER.info("Forwarding message: {}", forwardTextMessage.data);

                for (Session s : userSessionList) {
                    s.sendMessage(forwardTextMessage);
                    LOGGER.info("Message forwarded to session: {}", s.getIPString());
                }
            }
            case ONL -> {
                List<String> list = new ArrayList<>();
                for (Session session1 : userSessionList) {
                    String ipString = session1.getIPString();
                    list.add(ipString);
                }
                LOGGER.info("Online IPs: {}", list);
                session.sendMessage(new OnlineResponseMessage(request.getHeadersBuilder(), list));
            }
        }

    }
}
