package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.entity.EncryptedKeyEntity;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.exception.error.TooFewArgumentsException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.UUIDMessage;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import net.result.taulight.entity.ChatEntity;
import net.result.taulight.entity.MessageEntity;
import net.result.taulight.message.types.ForwardRequest;
import net.result.taulight.repository.MessageFileRepository;
import net.result.taulight.repository.MessageRepository;
import net.result.taulight.util.ChatUtil;
import net.result.taulight.util.TauHubProtocol;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ForwardRequestServerChain extends ServerChain implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(ForwardRequestServerChain.class);

    @Override
    public Message handle(RawMessage raw) {
        var chatUtil = session.server.container.get(ChatUtil.class);
        var messageRepo = session.server.container.get(MessageRepository.class);
        var messageFileRepo = session.server.container.get(MessageFileRepository.class);
        var jpaUtil = session.server.container.get(JPAUtil.class);

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
                .setNickname(session.member.getNickname());

        ChatEntity chat = chatUtil.getChat(chatID).orElseThrow(NotFoundException::new);
        if (!chatUtil.contains(chat, session.member.getTauMember())) throw new NotFoundException();
        MessageEntity message;
        if (input.keyID == null) {
            message = messageRepo.create(chat, input, session.member.getTauMember());
        } else {
            EncryptedKeyEntity key = jpaUtil
                    .find(EncryptedKeyEntity.class, input.keyID)
                    .orElseThrow(() -> new KeyStorageNotFoundException(input.keyID.toString()));
            message = messageRepo.create(chat, input, session.member.getTauMember(), key);
        }
        var viewDTO = message.toViewDTO(messageFileRepo);

        send(new UUIDMessage(new Headers().setType(MessageTypes.HAPPY), viewDTO.id));

        TauHubProtocol.send(session, chat, viewDTO);

        return forwardMessage.requireDeliveryAck ? new HappyMessage() : null;
    }
}
