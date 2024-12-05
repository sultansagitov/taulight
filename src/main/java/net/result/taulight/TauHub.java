package net.result.taulight;

import net.result.sandnode.exceptions.*;
import net.result.taulight.messages.TauMessageTypes;
import net.result.taulight.messages.OnlineResponseMessage;
import net.result.taulight.messages.types.TextMessage;
import net.result.sandnode.Hub;
import net.result.sandnode.config.IHubConfig;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.util.IMessageType;
import net.result.sandnode.server.Session;
import net.result.sandnode.util.db.IMember;
import net.result.sandnode.encryption.GlobalKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TauHub extends Hub {
    private static final Logger LOGGER = LogManager.getLogger(TauHub.class);

    public TauHub(GlobalKeyStorage hubKeyStorage, @NotNull IHubConfig hubConfig) {
        super(hubKeyStorage, hubConfig);
    }

    @Override
    public void onAgentMessage(
            @NotNull IMessage request,
            @NotNull Session session
    ) throws EncryptionException, KeyStorageNotFoundException, MessageSerializationException, MessageWriteException,
            IllegalMessageLengthException, UnexpectedSocketDisconnectException {

        IMessageType type = request.getHeaders().getType();
        if (type instanceof TauMessageTypes tau) {
            switch (tau) {
                case ECHO -> {
                    TextMessage echoTextMessage = new TextMessage(request);
                    LOGGER.info("Data: {}", echoTextMessage.data);
                    session.io.sendMessage(echoTextMessage);
                }
                case FWD -> {
                    TextMessage forwardTextMessage = new TextMessage(request);
                    LOGGER.info("Forwarding message: {}", forwardTextMessage.data);

                    for (Session s : session.server.groups.getSessions("fwd")) {
                        s.io.sendMessage(forwardTextMessage);
                        LOGGER.info("Message forwarded to session: {}", s);
                    }
                }
                case ONL -> {
                    Set<Session> fwd = session.server.groups.getSessions("fwd");
                    Set<IMember> list = fwd.stream().map(s -> s.member).collect(Collectors.toSet());
                    LOGGER.info("Online IPs: {}", list);
                    OnlineResponseMessage response = new OnlineResponseMessage(request.getHeaders(), list);
                    session.io.sendMessage(response);
                }
            }
        }
    }
}