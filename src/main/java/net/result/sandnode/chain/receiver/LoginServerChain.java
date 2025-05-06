package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.db.LoginRepository;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.LoginRequest;
import net.result.sandnode.message.types.LoginResponse;
import net.result.sandnode.security.Tokenizer;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.db.Database;

public class LoginServerChain extends ServerChain implements ReceiverChain {
    private final LoginRepository loginRepo = new LoginRepository();

    public LoginServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws Exception {
        RawMessage raw = queue.take();

        var msg = new LoginRequest(raw);
        String token = msg.content();

        Database database = session.server.serverConfig.database();
        Tokenizer tokenizer = session.server.serverConfig.tokenizer();

        session.member = tokenizer
                .findMember(database, token)
                .orElseThrow(UnauthorizedException::new);

        loginRepo.create(session.member, false);

        onLogin();

        sendFin(new LoginResponse(session.member));
    }

    protected void onLogin() throws Exception {}
}
