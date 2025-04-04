package net.result.taulight;

import net.result.sandnode.chain.ChainManager;
import net.result.sandnode.chain.IChain;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.types.ChainNameRequest;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.chain.sender.ForwardServerChain;
import net.result.taulight.db.ChatMessage;
import net.result.taulight.db.TauChat;
import net.result.taulight.db.ServerChatMessage;
import net.result.taulight.exception.AlreadyExistingRecordException;
import net.result.sandnode.exception.error.NoEffectException;
import net.result.taulight.group.TauGroupManager;
import net.result.taulight.message.types.ForwardResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Optional;

public class TauHubProtocol {
    private static final Logger LOGGER = LogManager.getLogger(TauHubProtocol.class);

    public static ServerChatMessage send(Session session, TauChat chat, ChatMessage chatMessage)
            throws InterruptedException, DatabaseException, NoEffectException, UnprocessedMessagesException,
            UnauthorizedException {
        if (session.member == null) {
            throw new UnauthorizedException();
        }

        TauGroupManager manager = (TauGroupManager) session.server.serverConfig.groupManager();

        ServerChatMessage serverMessage = new ServerChatMessage(chat.database());
        serverMessage.setCreationDateNow();
        serverMessage.setChatMessage(chatMessage);

        LOGGER.info("Saving message with id {} content: {}", serverMessage.id(), chatMessage.content());
        while (true) {
            serverMessage.setRandomID();
            try {
                serverMessage.save();
                break;
            } catch (AlreadyExistingRecordException ignored) {
            }
        }
        Collection<Session> sessions = manager.getGroup(chat).getSessions();
        if (sessions.isEmpty()) throw new NoEffectException();

        for (Session s : sessions) {
            serverMessage.setYourSession(s == session);
            ForwardResponse request = new ForwardResponse(serverMessage);

            ChainManager chainManager = s.io.chainManager;
            Optional<IChain> fwd = chainManager.getChain("fwd");

            if (fwd.isPresent()) {
                fwd.get().send(request);
            } else {
                var chain = new ForwardServerChain(s);
                chainManager.linkChain(chain);
                chain.send(request);
                chain.send(new ChainNameRequest("fwd"));
            }
        }

        serverMessage.setYourSession(false);

        return serverMessage;
    }
}
