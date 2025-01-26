package net.result.sandnode.chain.server;

import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.InvalidTokenException;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.types.LoginRequest;
import net.result.sandnode.message.types.LoginResponse;
import net.result.sandnode.message.types.TokenMessage;
import net.result.sandnode.error.Errors;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.db.IDatabase;
import net.result.sandnode.db.Member;

import java.util.Optional;

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
        Optional<Member> opt;

        try {
            opt = session.server.serverConfig.tokenizer().findMember(database, token);
        } catch (InvalidTokenException e) {
            sendFin(Errors.INVALID_TOKEN.message());
            return;
        }

        if (opt.isEmpty()) {
            sendFin(Errors.MEMBER_NOT_FOUND.message());
            return;
        }

        session.member = opt.get();

        sendFin(new LoginResponse(session.member));
    }
}
