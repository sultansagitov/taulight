package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.receiver.ServerChain;
import net.result.sandnode.error.Errors;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.SandnodeException;
import net.result.sandnode.exception.error.ServerSandnodeErrorException;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.code.InviteTauCode;
import net.result.taulight.db.InviteCodeObject;
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

        Optional<InviteCodeObject> optInviteCode;
        try {
            optInviteCode = database.getInviteCode(request.content());
        } catch (DatabaseException e) {
            throw new ServerSandnodeErrorException(e);
        }

        if (optInviteCode.isEmpty()) {
            sendFin(Errors.NOT_FOUND.createMessage());
            return;
        }

        InviteCodeObject invite = optInviteCode.get();

        Optional<TauChat> chat;
        try {
            chat = database.getChat(invite.getChatID());
        } catch (DatabaseException e) {
            throw new ServerSandnodeErrorException(e);
        }

        if (chat.isEmpty() || !(chat.get() instanceof TauChannel channel)) {
            sendFin(Errors.NOT_FOUND.createMessage());
            return;
        }

        var code = new InviteTauCode(invite, channel.title(), invite.getNickname(), invite.getSenderNickname());
        CheckCodeResponse response = new CheckCodeResponse(code);
        sendFin(response);
    }
}
