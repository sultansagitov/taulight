package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.receiver.ServerChain;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.db.TauChat;
import net.result.taulight.db.TauDatabase;
import net.result.taulight.message.types.MessageRequest;
import net.result.taulight.message.types.MessageResponse;

import java.util.List;

public class MessageServerChain extends ServerChain implements ReceiverChain {
    public MessageServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws Exception {
        TauDatabase database = (TauDatabase) session.server.serverConfig.database();

        RawMessage raw = queue.take();

        ServerErrorManager.instance().handleError(raw);

        MessageRequest request = new MessageRequest(raw);

        TauChat chat = database.getChat(request.getChatID()).orElseThrow(NotFoundException::new);

        if (!chat.getMembers().contains(session.member)) {
            throw new NotFoundException();
        }

        long count = chat.getMessageCount();
        List<ChatMessageViewDTO> messages = chat.loadMessages(request.getIndex(), request.getSize());

        sendFin(new MessageResponse(count, messages));
    }
}
