package net.result.taulight.chain.server;

import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.db.Member;
import net.result.sandnode.error.Errors;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.db.TauChat;
import net.result.taulight.db.TauDatabase;
import net.result.taulight.error.TauErrors;
import net.result.taulight.message.types.MessageRequest;
import net.result.taulight.message.types.MessageResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Optional;

public class MessageServerChain extends ServerChain {
    private static final Logger LOGGER = LogManager.getLogger(MessageServerChain.class);

    public MessageServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, DeserializationException, ExpectedMessageException,
            SandnodeErrorException, UnknownSandnodeErrorException {

        TauDatabase database = (TauDatabase) session.server.serverConfig.database();

        RawMessage raw = queue.take();

        if (raw.headers().type() == MessageTypes.ERR) {
            ErrorMessage errorMessage = new ErrorMessage(raw);
            ServerErrorManager.instance().throwAll(errorMessage.error);
        }

        MessageRequest request = new MessageRequest(raw);

        Optional<TauChat> chat;
        Collection<Member> members;

        try {
            chat = database.getChat(request.getChatID());

            if (chat.isEmpty()) {
                sendFin(TauErrors.CHAT_NOT_FOUND.createMessage());
                return;
            }

            members = chat.get().getMembers();

            if (!members.contains(session.member)) {
                sendFin(TauErrors.CHAT_NOT_FOUND.createMessage());
                return;
            }

            var messages = chat.get().loadMessages(request.getIndex(), request.getSize());

            sendFin(new MessageResponse(messages));

        } catch (DatabaseException e) {
            LOGGER.error("DB exception", e);
            sendFin(Errors.SERVER_ERROR.createMessage());
        }
    }
}
