package net.result.sandnode.chain.server;

import net.result.sandnode.exceptions.DeserializationException;
import net.result.sandnode.exceptions.ExpectedMessageException;
import net.result.sandnode.exceptions.InvalidTokenException;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.types.LoginRequest;
import net.result.sandnode.messages.types.LoginResponse;
import net.result.sandnode.messages.types.TokenMessage;
import net.result.sandnode.server.Session;
import net.result.sandnode.util.db.IDatabase;
import net.result.sandnode.util.db.IMember;

import java.util.Optional;

import static net.result.sandnode.server.ServerError.INVALID_TOKEN;
import static net.result.sandnode.server.ServerError.MEMBER_NOT_FOUND;

public class LoginServerChain extends ServerChain {
    public LoginServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, ExpectedMessageException, DeserializationException {
        IMessage request = queue.take();
        TokenMessage msg = new LoginRequest(request);
        String token = msg.getToken();

        IDatabase database = session.server.serverConfig.database();
        Optional<IMember> opt;

        try {
            opt = session.server.serverConfig.tokenizer().findMember(database, token);
        } catch (InvalidTokenException e) {
            sendFin(INVALID_TOKEN.message());
            return;
        }

        if (opt.isEmpty()) {
            sendFin(MEMBER_NOT_FOUND.message());
            return;
        }

        session.member = opt.get();

        sendFin(new LoginResponse(session.member));
    }
}
