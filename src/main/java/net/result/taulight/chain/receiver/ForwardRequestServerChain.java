package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.receiver.ServerChain;
import net.result.sandnode.error.Errors;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.TauHubProtocol;
import net.result.taulight.db.TauMemberEntity;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.db.TauDatabase;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.message.types.ForwardRequest;
import net.result.taulight.db.ChatEntity;
import net.result.sandnode.message.UUIDMessage;
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

            ChatMessageInputDTO input = forwardMessage.getChatMessageInputDTO();

            if (input == null) {
                LOGGER.error("Forward message contains null input");
                send(Errors.TOO_FEW_ARGS.createMessage());
                continue;
            }

            UUID chatID = input.chatID();
            String content = input.content();

            if (chatID == null || content == null) {
                LOGGER.error("Forward message contains null chatID or content");
                send(Errors.TOO_FEW_ARGS.createMessage());
                continue;
            }

            LOGGER.info("Forwarding message: {}", content);

            input
                .setSys(false)
                .setMember(session.member);

            ChatMessageViewDTO serverMessage;

            try {
                Optional<ChatEntity> chatOpt = database.getChat(chatID);

                if (chatOpt.isEmpty()) {
                    LOGGER.error("Chat was not found");
                    send(Errors.NOT_FOUND.createMessage());
                    continue;
                }

                ChatEntity chat = chatOpt.get();

                Collection<TauMemberEntity> members = database.getMembers(chat);
                if (!members.contains(session.member.tauMember())) {
                    LOGGER.warn("Unauthorized access attempt by member: {}", session.member.nickname());
                    send(Errors.NOT_FOUND.createMessage());
                    continue;
                }

                try {
                    serverMessage = TauHubProtocol.send(session, chat, input);
                } catch (UnauthorizedException e) {
                    throw new ImpossibleRuntimeException(e);
                }
            } catch (DatabaseException e) {
                LOGGER.error("Database error: {}", e.getMessage(), e);
                send(Errors.SERVER_ERROR.createMessage());
                continue;
            } catch (SandnodeErrorException e) {
                send(e.getSandnodeError().createMessage());
                continue;
            }

            send(new UUIDMessage(new Headers().setType(MessageTypes.HAPPY), serverMessage.id()));
        }
    }

}
