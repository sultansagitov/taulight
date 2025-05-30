package net.result.taulight.util;

import net.result.sandnode.chain.ChainManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.NoEffectException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.chain.sender.ForwardServerChain;
import net.result.taulight.db.ChatEntity;
import net.result.taulight.db.MessageEntity;
import net.result.taulight.db.MessageRepository;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.group.TauGroupManager;
import net.result.taulight.message.types.ForwardResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TauHubProtocol {
    private static final Logger LOGGER = LogManager.getLogger(TauHubProtocol.class);

    public static void send(Session session, ChatEntity chat, ChatMessageInputDTO input)
            throws InterruptedException, DatabaseException, SandnodeErrorException, UnprocessedMessagesException,
            ExpectedMessageException, UnknownSandnodeErrorException {
        if (session.member == null) throw new UnauthorizedException();

        MessageRepository messageRepo = session.server.container.get(MessageRepository.class);

        MessageEntity message = messageRepo.create(chat, input, session.member.tauMember());
        ChatMessageViewDTO serverMessage = new ChatMessageViewDTO(message);

        send(session, chat, serverMessage);
    }

    public static ChatMessageViewDTO send(Session session, ChatEntity chat, ChatMessageViewDTO serverMessage)
            throws InterruptedException, DatabaseException, SandnodeErrorException, UnprocessedMessagesException,
            ExpectedMessageException, UnknownSandnodeErrorException {
        if (session.member == null) throw new UnauthorizedException();

        TauGroupManager manager = session.server.container.get(TauGroupManager.class);

        Collection<Session> sessions = manager.getGroup(chat).getSessions();
        if (sessions.isEmpty()) throw new NoEffectException();

        ExecutorService executorService = null;
        try {
            //noinspection resource
            executorService = Executors.newFixedThreadPool(10);
            List<Future<?>> futures = new ArrayList<>();

            for (Session s : sessions) {
                futures.add(executorService.submit(() -> {
                    getObjectCallable(session, serverMessage, s);
                    return null;
                }));
            }

            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (ExecutionException e) {
                    try {
                        throw e.getCause();
                    } catch (UnprocessedMessagesException | InterruptedException | ExpectedMessageException |
                             UnknownSandnodeErrorException | SandnodeErrorException | DatabaseException ex) {
                        LOGGER.error("Error from executor", e);
                        throw ex;
                    } catch (Throwable ex) {
                        throw new ImpossibleRuntimeException(ex);
                    }
                }
            }
        } finally {
            if (executorService != null) {
                executorService.shutdown();
            }
        }

        LOGGER.info("Saved message with id {} content: {}", serverMessage.id, serverMessage.message.content);

        return serverMessage;
    }

    private static void getObjectCallable(Session session, ChatMessageViewDTO serverMessage, Session s)
            throws UnprocessedMessagesException, InterruptedException, ExpectedMessageException, UnknownSandnodeErrorException, SandnodeErrorException, DatabaseException {
        ForwardResponse request = new ForwardResponse(serverMessage, s == session);

        ChainManager chainManager = s.io.chainManager;

        var chain = new ForwardServerChain(s);
        chainManager.linkChain(chain);
        chain.response(request);
        chainManager.removeChain(chain);
    }
}
