package net.result.taulight.chain;

import net.result.sandnode.chain.Chain;

import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.exceptions.DeserializationException;
import net.result.sandnode.exceptions.ExpectedMessageException;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.util.MessageType;
import net.result.sandnode.server.Session;
import net.result.sandnode.util.db.IMember;
import net.result.sandnode.util.group.GroupManager;
import net.result.taulight.messages.OnlineResponseMessage;
import net.result.taulight.messages.TauMessageTypes;
import net.result.taulight.messages.types.EchoMessage;
import net.result.taulight.messages.types.ForwardMessage;
import net.result.taulight.messages.types.TimedForwardMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class TauHubServerChain extends ServerChain {
    private static final Logger LOGGER = LogManager.getLogger(TauHubServerChain.class);

    public TauHubServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, DeserializationException, ExpectedMessageException {
        while (io.isConnected()) {
            IMessage request = queue.take();
            MessageType type = request.getHeaders().getType();
            if (type instanceof TauMessageTypes tau) {
                final GroupManager groupManager = session.server.serverConfig.groupManager();
                switch (tau) {
                    case ECHO -> {
                        EchoMessage textMessage = new EchoMessage(request);
                        LOGGER.info("Data: {}", textMessage.data);
                        send(textMessage);
                    }
                    case FWD -> {
                        ForwardMessage forwardMessage;
                        try {
                            forwardMessage = new ForwardMessage(request);
                        } catch (DeserializationException e) {
                            LOGGER.error("Deserialization error", e);
                            throw new RuntimeException(e);
                        }
                        ZonedDateTime ztd = ZonedDateTime.now(ZoneId.of("UTC"));
                        LOGGER.info("Forwarding message: {}", forwardMessage.data);

                        for (Session s : groupManager.getGroup("chat").getSessions()) {
                            Optional<Chain> opt = s.io.chainManager.getChain("fwd");
                            if (opt.isPresent()) {
                                opt.get().send(new TimedForwardMessage(forwardMessage, ztd));
                            }
                            LOGGER.info("Message forwarded to session: {}", s);
                        }
                    }
                    case ONL -> {
                        Set<Session> fwd = groupManager.getGroup("chat").getSessions();
                        Set<IMember> list = fwd.stream().map(s -> s.member).collect(Collectors.toSet());
                        LOGGER.info("Online IPs: {}", list);
                        OnlineResponseMessage response = new OnlineResponseMessage(list);
                        send(response);
                    }
                }
            }
        }
    }
}