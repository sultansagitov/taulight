package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.RawMessage;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.entity.ChatEntity;
import net.result.taulight.entity.MessageEntity;
import net.result.taulight.message.types.MessageRequest;
import net.result.taulight.message.types.MessageResponse;
import net.result.taulight.repository.MessageFileRepository;
import net.result.taulight.repository.MessageRepository;
import net.result.taulight.util.ChatUtil;

import java.util.ArrayList;
import java.util.List;

public class MessageServerChain extends ServerChain implements ReceiverChain {
    @Override
    public MessageResponse handle(RawMessage raw) {
        ChatUtil chatUtil = session.server.container.get(ChatUtil.class);
        MessageRepository messageRepo = session.server.container.get(MessageRepository.class);
        MessageFileRepository messageFileRepo = session.server.container.get(MessageFileRepository.class);

        if (session.member == null) {
            throw new UnauthorizedException();
        }

        MessageRequest request = new MessageRequest(raw);

        ChatEntity chat = chatUtil.getChat(request.dto().chatID).orElseThrow(NotFoundException::new);

        if (!chatUtil.contains(chat, session.member.getTauMember())) {
            throw new NotFoundException();
        }

        long count = messageRepo.countMessagesByChat(chat);

        List<ChatMessageViewDTO> messages = new ArrayList<>();
        List<MessageEntity> entities = messageRepo.findMessagesByChat(chat, request.dto().index, request.dto().size);
        for (MessageEntity message : entities) {
            ChatMessageViewDTO chatMessageViewDTO = message.toViewDTO(messageFileRepo);
            messages.add(chatMessageViewDTO);
        }

        return new MessageResponse(count, messages);
    }
}
