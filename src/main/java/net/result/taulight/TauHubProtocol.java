package net.result.taulight;

import net.result.sandnode.chain.ChainManager;
import net.result.sandnode.chain.IChain;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.chain.sender.ForwardServerChain;
import net.result.taulight.db.MessageEntity;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.db.ChatEntity;
import net.result.taulight.db.TauDatabase;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.sandnode.exception.error.NoEffectException;
import net.result.taulight.group.TauGroupManager;
import net.result.taulight.message.types.ForwardResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class TauHubProtocol {
    private static final Logger LOGGER = LogManager.getLogger(TauHubProtocol.class);

    public static ChatMessageViewDTO send(Session session, ChatEntity chat, ChatMessageInputDTO input)
            throws InterruptedException, DatabaseException, NoEffectException, UnprocessedMessagesException,
            UnauthorizedException, NotFoundException {
        if (session.member == null) {
            throw new UnauthorizedException();
        }

        TauGroupManager manager = (TauGroupManager) session.server.serverConfig.groupManager();
        TauDatabase database = (TauDatabase) session.server.serverConfig.database();

        MessageEntity message = database.createMessage(chat, input, session.member);

        ChatMessageViewDTO serverMessage = new ChatMessageViewDTO(message);

        Collection<Session> sessions = manager.getGroup(chat).getSessions();
        if (sessions.isEmpty()) throw new NoEffectException();

        for (Session s : sessions) {
            ForwardResponse request = new ForwardResponse(serverMessage, s == session);

            ChainManager chainManager = s.io.chainManager;
            Optional<IChain> fwd = chainManager.getChain("fwd");

            if (fwd.isPresent()) {
                ((ForwardServerChain) fwd.get()).response(request);
            } else {
                var chain = new ForwardServerChain(s);
                chainManager.linkChain(chain);
                chain.response(request);
                chain.chainName("fwd");
            }
        }

        LOGGER.info("Saved message with id {} content: {}", serverMessage.id(), input.content());

        return serverMessage;
    }
}
