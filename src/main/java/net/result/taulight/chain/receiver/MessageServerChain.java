package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.receiver.ServerChain;
import net.result.sandnode.db.Member;
import net.result.sandnode.error.Errors;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.exception.error.ServerSandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.db.ServerChatMessage;
import net.result.taulight.db.TauChat;
import net.result.taulight.db.TauDatabase;
import net.result.taulight.message.types.MessageRequest;
import net.result.taulight.message.types.MessageResponse;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class MessageServerChain extends ServerChain implements ReceiverChain {
    public MessageServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, DeserializationException, ExpectedMessageException,
            SandnodeErrorException, UnknownSandnodeErrorException, UnprocessedMessagesException {

        TauDatabase database = (TauDatabase) session.server.serverConfig.database();

        RawMessage raw = queue.take();

        ServerErrorManager.instance().handleError(raw);

        MessageRequest request = new MessageRequest(raw);

        Optional<TauChat> chat;
        Collection<Member> members;

        try {
            chat = database.getChat(request.getChatID());

            if (chat.isEmpty()) {
                sendFin(Errors.NOT_FOUND.createMessage());
                return;
            }

            members = chat.get().getMembers();

            if (!members.contains(session.member)) {
                sendFin(Errors.NOT_FOUND.createMessage());
                return;
            }

            long count = chat.get().getMessageCount();
            List<ServerChatMessage> messages = chat.get().loadMessages(request.getIndex(), request.getSize());

            sendFin(new MessageResponse(count, messages));

        } catch (DatabaseException e) {
            throw new ServerSandnodeErrorException(e);
        }
    }
}
