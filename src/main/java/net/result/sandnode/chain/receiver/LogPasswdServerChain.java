package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.db.LoginEntity;
import net.result.sandnode.db.LoginRepository;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.db.MemberRepository;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.hubagent.Hub;
import net.result.sandnode.message.types.*;
import net.result.sandnode.security.PasswordHasher;
import net.result.sandnode.security.Tokenizer;
import net.result.sandnode.serverclient.Session;

public abstract class LogPasswdServerChain extends ServerChain implements ReceiverChain {

    public LogPasswdServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws Exception {
        LogPasswdRequest request = new LogPasswdRequest(queue.take());

        Tokenizer tokenizer = session.server.container.get(Tokenizer.class);
        LoginRepository loginRepo = session.server.container.get(LoginRepository.class);
        MemberRepository memberRepo = session.server.container.get(MemberRepository.class);
        Hub hub = (Hub) session.server.node;
        PasswordHasher hasher = hub.config.hasher();

        MemberEntity member = memberRepo
                .findByNickname(request.getNickname())
                .orElseThrow(UnauthorizedException::new);

        boolean verified = hasher.verify(request.getPassword(), member.hashedPassword());
        if (!verified) {
            throw new UnauthorizedException();
        }

        session.member = member;

        String ip = session.io.socket.getInetAddress().getHostAddress();
        LoginEntity login = loginRepo.create(session.member, ip, request.getDevice());

        onLogin();

        String token = tokenizer.tokenizeLogin(login);
        sendFin(new LogPasswdResponse(token));
    }

    protected abstract void onLogin() throws Exception;
}
