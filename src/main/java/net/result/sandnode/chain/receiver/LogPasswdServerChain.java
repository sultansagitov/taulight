package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.db.Database;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.*;
import net.result.sandnode.serverclient.Session;

public abstract class LogPasswdServerChain extends ServerChain implements ReceiverChain {
    public LogPasswdServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws Exception {
        RawMessage request = queue.take();
        LogPasswdRequest msg = new LogPasswdRequest(request);

        Database database = session.server.serverConfig.database();

        MemberEntity member = database
                .findMemberByNickname(msg.getNickname())
                .orElseThrow(UnauthorizedException::new);

        boolean verified = database.hasher().verify(msg.getPassword(), member.hashedPassword());
        if (!verified) {
            throw new UnauthorizedException();
        }

        session.member = member;

        onLogin();

        String token = session.server.serverConfig.tokenizer().tokenizeMember(session.member);
        sendFin(new LogPasswdResponse(token));
    }

    protected abstract void onLogin() throws Exception;
}
