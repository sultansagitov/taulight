package net.result.taulight.util;

import net.result.sandnode.chain.ChainManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.NoEffectException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.chain.sender.ForwardServerChain;
import net.result.taulight.cluster.TauClusterManager;
import net.result.taulight.entity.ChatEntity;
import net.result.taulight.entity.MessageEntity;
import net.result.taulight.repository.MessageFileRepository;
import net.result.taulight.repository.MessageRepository;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.message.types.ForwardResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TauHubProtocol {
    private static final Logger LOGGER = LogManager.getLogger(TauHubProtocol.class);

    public static void send(Session session, ChatEntity chat, ChatMessageInputDTO input) {
        if (session.member == null) throw new UnauthorizedException();

        var messageRepo = session.server.container.get(MessageRepository.class);
        var messageFileRepo = session.server.container.get(MessageFileRepository.class);

        MessageEntity message = messageRepo.create(chat, input, session.member.tauMember());
        LOGGER.info("Saved message with id {} content: {}", message.id(), message.content());
        ChatMessageViewDTO serverMessage = message.toViewDTO(messageFileRepo);

        send(session, chat, serverMessage);
    }

    public static void send(Session session, ChatEntity chat, ChatMessageViewDTO serverMessage) {
        if (session.member == null) throw new UnauthorizedException();

        var manager = session.server.container.get(TauClusterManager.class);

        var sessions = manager.getCluster(chat).getSessions();
        if (sessions.isEmpty()) throw new NoEffectException();

        ExecutorService executorService = null;
        try {
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
                } catch (InterruptedException e) {
                    throw new SandnodeInterruptedException(e);
                }
            }
        } finally {
            if (executorService != null) {
                executorService.shutdown();
            }
        }
    }

    private static void getObjectCallable(Session session, ChatMessageViewDTO serverMessage, Session s) {
        ForwardResponse request = new ForwardResponse(serverMessage, s == session);

        ChainManager chainManager = s.io().chainManager;

        var chain = new ForwardServerChain(s);
        chainManager.linkChain(chain);
        chain.response(request);
        chainManager.removeChain(chain);
    }

}
