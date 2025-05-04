package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.db.Database;
import net.result.sandnode.exception.SandnodeException;
import net.result.sandnode.exception.error.InvalidNicknamePassword;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.RegistrationRequest;
import net.result.sandnode.message.types.RegistrationResponse;
import net.result.sandnode.security.Tokenizer;
import net.result.sandnode.serverclient.Session;

public class RegistrationServerChain extends ServerChain implements ReceiverChain {
    public RegistrationServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, SandnodeException {
        RawMessage request = queue.take();

        Database database = session.server.serverConfig.database();
        Tokenizer tokenizer = session.server.serverConfig.tokenizer();

        RegistrationRequest regMsg = new RegistrationRequest(request);
        String nickname = regMsg.getNickname();
        String password = regMsg.getPassword();

        if (nickname.isEmpty() || password.isEmpty()) {
            throw new InvalidNicknamePassword();
        }

        session.member = database.registerMember(nickname, password);
        String token = tokenizer.tokenizeMember(session.member);
        sendFin(new RegistrationResponse(token));
    }
}
