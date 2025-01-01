package net.result.taulight.chain;

import net.result.sandnode.chain.Chain;

import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.util.IMessageType;
import net.result.sandnode.server.Session;
import net.result.sandnode.util.db.IMember;
import net.result.sandnode.util.group.IGroupManager;
import net.result.taulight.messages.OnlineResponseMessage;
import net.result.taulight.messages.TauMessageTypes;
import net.result.taulight.messages.types.TextMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class TauHubServerChain extends ServerChain {
    private static final Logger LOGGER = LogManager.getLogger(TauHubServerChain.class);

    public TauHubServerChain(Session session) {
        super(session);
    }

    @Override
    public void start() throws InterruptedException {
        while (io.isConnected()) {
            IMessage request = queue.take();
            IMessageType type = request.getHeaders().getType();
            if (type instanceof TauMessageTypes tau) {
                final IGroupManager groupManager = session.server.serverConfig.groupManager();
                switch (tau) {
                    case ECHO -> {
                        TextMessage textMessage = new TextMessage(request);
                        LOGGER.info("Data: {}", textMessage.data);
                        send(textMessage);
                    }
                    case FWD -> {
                        TextMessage textMessage = new TextMessage(request);
                        LOGGER.info("Forwarding message: {}", textMessage.data);

                        for (Session s : groupManager.getGroup("chat").getSessions()) {
                            Optional<Chain> opt = s.io.chainManager.getChain("fwd");
                            if (opt.isPresent()) opt.get().send(textMessage);
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