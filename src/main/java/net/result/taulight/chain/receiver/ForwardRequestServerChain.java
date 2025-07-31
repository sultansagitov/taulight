package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.db.EncryptedKeyEntity;
import net.result.sandnode.db.EncryptedKeyRepository;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.exception.error.TooFewArgumentsException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.UUIDMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.db.ChatEntity;
import net.result.taulight.db.MessageEntity;
import net.result.taulight.db.MessageFileRepository;
import net.result.taulight.db.MessageRepository;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.message.types.ForwardRequest;
import net.result.taulight.util.ChatUtil;
import net.result.taulight.util.TauHubProtocol;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ForwardRequestServerChain extends ServerChain implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(ForwardRequestServerChain.class);

    public ForwardRequestServerChain(Session session) {
        super(session);
    }

    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public Message handle(RawMessage raw) throws Exception {
        ChatUtil chatUtil = session.server.container.get(ChatUtil.class);
        MessageRepository messageRepo = session.server.container.get(MessageRepository.class);
        EncryptedKeyRepository encryptedKeyRepo = session.server.container.get(EncryptedKeyRepository.class);
        MessageFileRepository messageFileRepo = session.server.container.get(MessageFileRepository.class);

        while (true) {
            if (raw.headers().type() == MessageTypes.ERR) {
                LOGGER.error("Error {}", new ErrorMessage(raw).error);
                continue;
            }

            var forwardMessage = new ForwardRequest(raw);

            if (session.member == null) throw new UnauthorizedException();

            var input = forwardMessage.getChatMessageInputDTO();
            if (input == null) throw new TooFewArgumentsException();

            var chatID = input.chatID;
            var content = input.content;
            if (chatID == null || content == null) throw new TooFewArgumentsException();

            LOGGER.info("Forwarding message: {}", content);

            input
                    .setSys(false)
                    .setMember(session.member);

            ChatEntity chat = chatUtil.getChat(chatID).orElseThrow(NotFoundException::new);
            if (!chatUtil.contains(chat, session.member.tauMember())) throw new NotFoundException();
            MessageEntity message;
            if (input.keyID == null) {
                message = messageRepo.create(chat, input, session.member.tauMember());
            } else {
                EncryptedKeyEntity key = encryptedKeyRepo
                        .find(input.keyID)
                        .orElseThrow(() -> new KeyStorageNotFoundException(input.keyID.toString()));
                message = messageRepo.create(chat, input, session.member.tauMember(), key);
            }
            ChatMessageViewDTO viewDTO = new ChatMessageViewDTO(messageFileRepo, message);

            send(new UUIDMessage(new Headers().setType(MessageTypes.HAPPY), viewDTO.id));

            TauHubProtocol.send(session, chat, viewDTO);

            if (forwardMessage.requireDeliveryAck) {
                raw = sendAndReceive(new HappyMessage());
            }
        }
    }
}
