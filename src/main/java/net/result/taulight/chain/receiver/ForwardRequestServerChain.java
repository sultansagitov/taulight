package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.receiver.ServerChain;
import net.result.sandnode.db.Member;
import net.result.sandnode.error.Errors;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.TauHubProtocol;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.db.TauDatabase;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.sandnode.exception.error.NoEffectException;
import net.result.taulight.message.types.ForwardRequest;
import net.result.taulight.db.TauChat;
import net.result.taulight.message.types.UUIDMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class ForwardRequestServerChain extends ServerChain implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(ForwardRequestServerChain.class);

    public ForwardRequestServerChain(Session session) {
        super(session);
    }

    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void sync() throws InterruptedException, SandnodeException {
        TauDatabase database = (TauDatabase) session.server.serverConfig.database();

        while (true) {
            RawMessage raw = queue.take();

            if (raw.headers().type() == MessageTypes.ERR) {
                LOGGER.error("Error {}", new ErrorMessage(raw).error);
                continue;
            }

            ForwardRequest forwardMessage = new ForwardRequest(raw);

            if (session.member == null) {
                send(Errors.UNAUTHORIZED.createMessage());
                continue;
            }

            ChatMessageInputDTO chatMessage = forwardMessage.getChatMessage();

            if (chatMessage == null) {
                LOGGER.error("Forward message contains null chatMessage");
                send(Errors.TOO_FEW_ARGS.createMessage());
                continue;
            }

            UUID chatID = chatMessage.chatID();
            String content = chatMessage.content();

            if (chatID == null || content == null) {
                LOGGER.error("Forward message contains null chatID or content");
                send(Errors.TOO_FEW_ARGS.createMessage());
                continue;
            }

            LOGGER.info("Forwarding message: {}", content);

            chatMessage
                .setSys(false)
                .setMember(session.member);

            ChatMessageViewDTO serverMessage;

            try {
                Optional<TauChat> chatOpt = database.getChat(chatID);

                if (chatOpt.isEmpty()) {
                    LOGGER.error("Chat was not found");
                    send(Errors.NOT_FOUND.createMessage());
                    continue;
                }

                TauChat chat = chatOpt.get();

                Collection<Member> members = chat.getMembers();
                if (!members.contains(session.member)) {
                    LOGGER.warn("Unauthorized access attempt by member: {}", session.member);
                    send(Errors.NOT_FOUND.createMessage());
                    continue;
                }

                try {
                    serverMessage = TauHubProtocol.send(session, chat, chatMessage);
                } catch (UnauthorizedException e) {
                    throw new ImpossibleRuntimeException(e);
                }
            } catch (DatabaseException e) {
                LOGGER.error("Database error: {}", e.getMessage(), e);
                send(Errors.SERVER_ERROR.createMessage());
                continue;
            } catch (NoEffectException e) {
                LOGGER.error("Message forwarding failed for chat: {}", chatID, e);
                send(Errors.NO_EFFECT.createMessage());
                continue;
            }

            send(new UUIDMessage(new Headers().setType(MessageTypes.HAPPY), serverMessage.id()));
        }
    }

}
