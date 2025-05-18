package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.db.LoginEntity;
import net.result.sandnode.db.LoginRepository;
import net.result.sandnode.db.MemberRepository;
import net.result.sandnode.exception.SandnodeException;
import net.result.sandnode.exception.error.InvalidNicknamePassword;
import net.result.sandnode.hubagent.Hub;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.RegistrationRequest;
import net.result.sandnode.message.types.RegistrationResponse;
import net.result.sandnode.security.PasswordHasher;
import net.result.sandnode.security.Tokenizer;
import net.result.sandnode.serverclient.Session;

public class RegistrationServerChain extends ServerChain implements ReceiverChain {
    public RegistrationServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, SandnodeException {
        RawMessage request = queue.take();

        MemberRepository memberRepo = session.server.container.get(MemberRepository.class);
        LoginRepository loginRepo = session.server.container.get(LoginRepository.class);
        Tokenizer tokenizer = session.server.container.get(Tokenizer.class);
        Hub hub = (Hub) session.server.node;
        PasswordHasher hasher = hub.config.hasher();

        RegistrationRequest regMsg = new RegistrationRequest(request);
        String nickname = regMsg.getNickname();
        String password = regMsg.getPassword();
        String device = regMsg.getDevice();

        if (nickname.isEmpty() || password.isEmpty()) {
            throw new InvalidNicknamePassword();
        }

        session.member = memberRepo.create(nickname, hasher.hash(password, 12));

        String ip = session.io.socket.getInetAddress().getHostAddress();
        LoginEntity login = loginRepo.create(session.member, ip, device);

        String token = tokenizer.tokenizeLogin(login);
        sendFin(new RegistrationResponse(token));
    }
}
