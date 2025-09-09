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
import net.result.sandnode.message.types.HappyMessage;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.entity.ChatEntity;
import net.result.taulight.entity.MessageEntity;
import net.result.taulight.entity.TauMemberEntity;
import net.result.taulight.message.types.UpstreamRequest;
import net.result.taulight.message.types.UpstreamResponse;
import net.result.taulight.repository.MessageFileRepository;
import net.result.taulight.repository.MessageRepository;
import net.result.taulight.repository.TauMemberRepository;
import net.result.taulight.util.ChatUtil;
import net.result.taulight.util.TauHubProtocol;

public class UpstreamServerChain extends ServerChain implements ReceiverChain {
    @Override
    public Message handle(RawMessage raw) {
        final var jpaUtil = session.server.container.get(JPAUtil.class);
        final var chatUtil = session.server.container.get(ChatUtil.class);
        final var tauMemberRepo = session.server.container.get(TauMemberRepository.class);
        final var messageRepo = session.server.container.get(MessageRepository.class);
        final var messageFileRepo = session.server.container.get(MessageFileRepository.class);

        final var upstreamReq = new UpstreamRequest(raw);

        if (session.member == null) throw new UnauthorizedException();

        final var input = upstreamReq.getChatMessageInputDTO();
        if (input == null) throw new TooFewArgumentsException();

        final var chatID = input.chatID;
        final var content = input.content;
        if (chatID == null || content == null) throw new TooFewArgumentsException();

        input
                .setSys(false)
                .setNickname(session.member.getNickname());

        ChatEntity chat = chatUtil.getChat(chatID).orElseThrow(NotFoundException::new);
        TauMemberEntity tauMember = tauMemberRepo.findByMember(session.member);
        if (!chatUtil.contains(chat, tauMember)) throw new NotFoundException();
        MessageEntity message;
        if (input.keyID == null) {
            message = messageRepo.create(chat, input, tauMember);
        } else {
            EncryptedKeyEntity key = jpaUtil
                    .find(EncryptedKeyEntity.class, input.keyID)
                    .orElseThrow(() -> new KeyStorageNotFoundException(input.keyID.toString()));
            message = messageRepo.create(chat, input, tauMember, key);
        }
        ChatMessageViewDTO viewDTO = message.toViewDTO(messageFileRepo);

        send(new UpstreamResponse(viewDTO));

        TauHubProtocol.send(session, chat, viewDTO);

        return upstreamReq.requireDeliveryAck ? new HappyMessage() : null;
    }
}
