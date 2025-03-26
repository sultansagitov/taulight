package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.receiver.ServerChain;
import net.result.sandnode.exception.SandnodeException;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.code.InviteTauCode;
import net.result.taulight.db.InviteCodeObject;
import net.result.taulight.db.TauChannel;
import net.result.taulight.db.TauChat;
import net.result.taulight.db.TauDatabase;
import net.result.taulight.message.types.CheckCodeRequest;
import net.result.taulight.message.types.CheckCodeResponse;

import java.util.UUID;

public class CheckCodeServerChain extends ServerChain implements ReceiverChain {
    public CheckCodeServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, SandnodeException {
        CheckCodeRequest request = new CheckCodeRequest(queue.take());

        if (session.member == null) {
            throw new UnauthorizedException();
        }

        TauDatabase database = (TauDatabase) session.member.database();

        InviteCodeObject invite = database
                .getInviteCode(request.content())
                .orElseThrow(NotFoundException::new);

        UUID chatID = invite.getChatID();

        TauChat chat = database.getChat(chatID).orElseThrow(NotFoundException::new);

        if (!(chat instanceof TauChannel channel)) {
            throw new NotFoundException();
        }

        var code = new InviteTauCode(invite, channel.title(), invite.getNickname(), invite.getSenderNickname());
        CheckCodeResponse response = new CheckCodeResponse(code);
        sendFin(response);
    }
}
