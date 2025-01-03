package net.result.sandnode.chain.server;

import net.result.sandnode.exceptions.ExpectedMessageException;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.types.LoginRequest;
import net.result.sandnode.messages.types.LoginResponse;
import net.result.sandnode.messages.types.TokenMessage;
import net.result.sandnode.server.Session;
import net.result.sandnode.util.db.IDatabase;
import net.result.sandnode.util.db.IMember;

import java.util.Optional;

import static net.result.sandnode.server.ServerError.MEMBER_NOT_FOUND;

public class LoginServerChain extends ServerChain {
    public LoginServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, ExpectedMessageException {
        IMessage request = queue.take();
        TokenMessage msg = new LoginRequest(request);
        String token = msg.getToken();

        IDatabase database = session.server.serverConfig.database();
        Optional<IMember> opt = session.server.serverConfig.tokenizer().findMember(database, token);

        if (opt.isEmpty()) {
            sendFin(MEMBER_NOT_FOUND.message());
            return;
        }

        session.member = opt.get();

        sendFin(new LoginResponse(session.member));
    }
}
