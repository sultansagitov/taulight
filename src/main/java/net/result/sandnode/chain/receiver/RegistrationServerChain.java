package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
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
        Tokenizer tokenizer = session.server.container.get(Tokenizer.class);
        Hub hub = (Hub) session.server.node;
        PasswordHasher hasher = hub.config.hasher();

        RegistrationRequest regMsg = new RegistrationRequest(request);
        String nickname = regMsg.getNickname();
        String password = regMsg.getPassword();

        if (nickname.isEmpty() || password.isEmpty()) {
            throw new InvalidNicknamePassword();
        }

        session.member = memberRepo.create(nickname, hasher.hash(password, 12));
        String token = tokenizer.tokenizeMember(session.member);
        sendFin(new RegistrationResponse(token));
    }
}
