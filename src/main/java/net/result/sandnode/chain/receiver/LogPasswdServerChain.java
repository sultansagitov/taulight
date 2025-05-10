package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.db.LoginRepository;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.db.MemberRepository;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.*;
import net.result.sandnode.security.PasswordHasher;
import net.result.sandnode.serverclient.Session;

public abstract class LogPasswdServerChain extends ServerChain implements ReceiverChain {

    public LogPasswdServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws Exception {
        RawMessage request = queue.take();
        LogPasswdRequest msg = new LogPasswdRequest(request);

        LoginRepository loginRepo = session.server.container.get(LoginRepository.class);
        MemberRepository memberRepo = session.server.container.get(MemberRepository.class);
        PasswordHasher hasher = session.server.serverConfig.hasher();

        MemberEntity member = memberRepo
                .findByNickname(msg.getNickname())
                .orElseThrow(UnauthorizedException::new);

        boolean verified = hasher.verify(msg.getPassword(), member.hashedPassword());
        if (!verified) {
            throw new UnauthorizedException();
        }

        session.member = member;

        String ip = session.io.socket.getInetAddress().getHostAddress();
        loginRepo.create(session.member, ip, true);

        onLogin();

        String token = session.server.serverConfig.tokenizer().tokenizeMember(session.member);
        sendFin(new LogPasswdResponse(token));
    }

    protected abstract void onLogin() throws Exception;
}
