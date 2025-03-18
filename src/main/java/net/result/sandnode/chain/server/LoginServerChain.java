package net.result.sandnode.chain.server;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.ExpiredTokenException;
import net.result.sandnode.exception.error.InvalidTokenException;
import net.result.sandnode.exception.error.ServerSandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.LoginRequest;
import net.result.sandnode.message.types.LoginResponse;
import net.result.sandnode.message.types.TokenMessage;
import net.result.sandnode.error.Errors;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.db.Database;
import net.result.sandnode.db.Member;

import java.util.Optional;

public class LoginServerChain extends ServerChain implements ReceiverChain {
    public LoginServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, SandnodeException {
        RawMessage raw = queue.take();

        if (session.member == null) {
            sendFin(Errors.UNAUTHORIZED.createMessage());
            return;
        }

        TokenMessage msg = new LoginRequest(raw);
        String token = msg.getToken();

        Database database = session.server.serverConfig.database();
        Optional<Member> opt;

        try {
            opt = session.server.serverConfig.tokenizer().findMember(database, token);
        } catch (InvalidTokenException e) {
            sendFin(Errors.INVALID_TOKEN.createMessage());
            return;
        } catch (ExpiredTokenException e) {
            sendFin(Errors.EXPIRED_TOKEN.createMessage());
            return;
        } catch (DatabaseException e) {
            throw new ServerSandnodeErrorException(e);
        }

        if (opt.isEmpty()) {
            sendFin(Errors.UNAUTHORIZED.createMessage());
            return;
        }

        session.member = opt.get();

        onLogin();

        sendFin(new LoginResponse(session.member));
    }

    protected void onLogin() throws InterruptedException, SandnodeException {}
}
