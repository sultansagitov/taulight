package net.result.taulight.chain.server;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.error.Errors;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.SandnodeException;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.code.InviteTauCode;
import net.result.taulight.db.InviteToken;
import net.result.taulight.db.TauChannel;
import net.result.taulight.db.TauChat;
import net.result.taulight.db.TauDatabase;
import net.result.taulight.message.types.CheckCodeRequest;
import net.result.taulight.message.types.CheckCodeResponse;

import java.util.Optional;

public class CheckCodeServerChain extends ServerChain implements ReceiverChain {
    public CheckCodeServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, SandnodeException {
        CheckCodeRequest request = new CheckCodeRequest(queue.take());

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

        InviteTauCode code = new InviteTauCode(it, channel.title(), it.getNickname(), it.getSenderNickname());
        CheckCodeResponse response = new CheckCodeResponse(code);
        sendFin(response);
    }
}
