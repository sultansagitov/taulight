package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.receiver.ServerChain;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.db.ChatEntity;
import net.result.taulight.db.TauDatabase;
import net.result.taulight.message.types.MessageRequest;
import net.result.taulight.message.types.MessageResponse;

import java.util.List;
import java.util.stream.Collectors;

public class MessageServerChain extends ServerChain implements ReceiverChain {
    public MessageServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws Exception {
        TauDatabase database = (TauDatabase) session.server.serverConfig.database();

        if (session.member == null) {
            throw new UnauthorizedException();
        }

        RawMessage raw = queue.take();

        ServerErrorManager.instance().handleError(raw);

        MessageRequest request = new MessageRequest(raw);

        ChatEntity chat = database.getChat(request.getChatID()).orElseThrow(NotFoundException::new);

        if (database.getMembers(chat).stream().noneMatch(m -> m.id().equals(session.member.id()))) {
            throw new NotFoundException();
        }

        long count = database.getMessageCount(chat);

        List<ChatMessageViewDTO> messages = database
                .loadMessages(chat, request.getIndex(), request.getSize()).stream()
                .map(ChatMessageViewDTO::new)
                .collect(Collectors.toList());

        sendFin(new MessageResponse(count, messages));
    }
}
