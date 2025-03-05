package net.result.taulight;

import net.result.sandnode.chain.Chain;
import net.result.sandnode.chain.ChainManager;
import net.result.sandnode.config.ServerConfig;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.message.types.ChainNameRequest;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.chain.server.ForwardServerChain;
import net.result.taulight.db.ChatMessage;
import net.result.taulight.db.TauChat;
import net.result.taulight.db.TauDatabase;
import net.result.taulight.db.ServerChatMessage;
import net.result.taulight.exception.AlreadyExistingRecordException;
import net.result.taulight.exception.error.MessageNotForwardedException;
import net.result.taulight.group.TauGroupManager;
import net.result.taulight.message.types.ForwardResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Optional;

public class TauHubProtocol {
    private static final Logger LOGGER = LogManager.getLogger(TauHubProtocol.class);

    public static ServerChatMessage send(ServerConfig serverConfig, TauChat chat, ChatMessage chatMessage)
            throws InterruptedException, DatabaseException, MessageNotForwardedException {
        TauDatabase database = (TauDatabase) serverConfig.database();
        TauGroupManager manager = (TauGroupManager) serverConfig.groupManager();

        ServerChatMessage serverMessage = new ServerChatMessage();
        serverMessage.setCreationDateNow();
        serverMessage.setChatMessage(chatMessage);

        LOGGER.info("Saving message with id {} content: {}", serverMessage.id(), chatMessage.content());
        while (true) {
            serverMessage.setRandomID();
            try {
                database.saveMessage(serverMessage);
                break;
            } catch (AlreadyExistingRecordException ignored) {
            }
        }
        Collection<Session> sessions = manager.getGroup(chat).getSessions();
        if (sessions.isEmpty()) throw new MessageNotForwardedException();

        for (Session session : sessions) {
            ForwardResponse request = new ForwardResponse(serverMessage);

            ChainManager chainManager = session.io.chainManager;
            Optional<Chain> fwd = chainManager.getChain("fwd");

            if (fwd.isPresent()) {
                fwd.get().send(request);
            } else {
                var chain = new ForwardServerChain(session);
                chainManager.linkChain(chain);
                chain.send(request);
                chain.send(new ChainNameRequest("fwd"));
            }
        }

        return serverMessage;
    }
}
