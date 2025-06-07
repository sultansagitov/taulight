package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.db.ChatEntity;
import net.result.taulight.db.MessageRepository;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.message.types.MessageRequest;
import net.result.taulight.message.types.MessageResponse;
import net.result.taulight.util.ChatUtil;

import java.util.List;
import java.util.stream.Collectors;

public class MessageServerChain extends ServerChain implements ReceiverChain {
    public MessageServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws Exception {
        ChatUtil chatUtil = session.server.container.get(ChatUtil.class);
        MessageRepository messageRepo = session.server.container.get(MessageRepository.class);

        if (session.member == null) {
            throw new UnauthorizedException();
        }

        RawMessage raw = queue.take();

        ServerErrorManager.instance().handleError(raw);

        MessageRequest request = new MessageRequest(raw);

        ChatEntity chat = chatUtil.getChat(request.dto().chatID).orElseThrow(NotFoundException::new);

        if (!chatUtil.contains(chat, session.member.tauMember())) {
            throw new NotFoundException();
        }

        long count = messageRepo.countMessagesByChat(chat);

        List<ChatMessageViewDTO> messages = messageRepo
                .findMessagesByChat(chat, request.dto().index, request.dto().size).stream()
                .map(ChatMessageViewDTO::new)
                .collect(Collectors.toList());

        sendFin(new MessageResponse(count, messages));
    }
}
