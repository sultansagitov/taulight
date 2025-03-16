package net.result.taulight.chain.server;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.error.Errors;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.db.InviteToken;
import net.result.taulight.db.TauChannel;
import net.result.taulight.db.TauChat;
import net.result.taulight.db.TauDatabase;
import net.result.taulight.message.types.TauCodeRequest;
import net.result.taulight.message.types.TauCodeResponse;

import java.util.Optional;

public class CodeServerChain extends ServerChain implements ReceiverChain {
    public CodeServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, DeserializationException, UnprocessedMessagesException {
        TauCodeRequest request = new TauCodeRequest(queue.take());
        String mode = request.headers()
                .getOptionalValue("mode")
                .orElseThrow(() -> new DeserializationException(""));//TODO write message

        switch (mode) {
            case "use" -> use(request);
            case "check" -> check(request);
            default -> throw new DeserializationException(""); // Todo
        }
    }

    private void check(TauCodeRequest request) throws UnprocessedMessagesException, InterruptedException {
        if (session.member == null) {
            sendFin(Errors.UNAUTHORIZED.createMessage());
            return;
        }

        TauDatabase database = (TauDatabase) session.member.database();

        Optional<InviteToken> inviteToken;
        try {
            inviteToken = database.getInviteToken(request.content());
        } catch (DatabaseException e) {
            sendFin(Errors.SERVER_ERROR.createMessage());
            return;
        }

        if (inviteToken.isEmpty()) {
            sendFin(Errors.NOT_FOUND.createMessage());
            return;
        }

        InviteToken it = inviteToken.get();

        Optional<TauChat> chat;
        try {
            chat = database.getChat(it.getChatID());
        } catch (DatabaseException e) {
            sendFin(Errors.SERVER_ERROR.createMessage());
            return;
        }

        if (chat.isEmpty() || !(chat.get() instanceof TauChannel channel)) {
            sendFin(Errors.NOT_FOUND.createMessage());
            return;
        }

        TauCodeResponse.Data data = new TauCodeResponse.Data(it, channel);
        TauCodeResponse response = new TauCodeResponse(data);
        sendFin(response);
    }

    private void use(TauCodeRequest request) {
        // TODO realize it
    }
}
