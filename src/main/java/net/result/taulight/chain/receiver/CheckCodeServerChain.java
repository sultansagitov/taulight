package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.receiver.ServerChain;
import net.result.sandnode.exception.SandnodeException;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.db.TauMemberEntity;
import net.result.taulight.dto.InviteCodeDTO;
import net.result.taulight.db.InviteCodeEntity;
import net.result.taulight.db.TauDatabase;
import net.result.taulight.message.types.CheckCodeRequest;
import net.result.taulight.message.types.CheckCodeResponse;

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

        TauMemberEntity tauMember = session.member.tauMember();

        TauDatabase database = (TauDatabase) session.server.serverConfig.database();

        InviteCodeEntity invite = database
                .findInviteCode(request.content())
                .orElseThrow(NotFoundException::new);

        if (invite.receiver() != tauMember) {
            //TODO add channel roles and use it
            if (invite.sender() != tauMember) {
                throw new NotFoundException();
            }
        }

        var code = new InviteCodeDTO(invite);
        sendFin(new CheckCodeResponse(code));
    }
}
