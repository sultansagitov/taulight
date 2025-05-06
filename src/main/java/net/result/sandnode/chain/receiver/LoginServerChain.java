package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.db.LoginEntity;
import net.result.sandnode.db.LoginRepository;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.LoginHistoryResponse;
import net.result.sandnode.message.types.LoginRequest;
import net.result.sandnode.message.types.LoginResponse;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.security.Tokenizer;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.db.Database;

import java.util.List;

public class LoginServerChain extends ServerChain implements ReceiverChain {
    private final LoginRepository loginRepo = new LoginRepository();

    public LoginServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws Exception {
        RawMessage raw = queue.take();

        var request = new LoginRequest(raw);

        if (request.headers().getOptionalValue("history").map(h -> h.equals("true")).orElse(false)) {
            history();
        } else {
            loginByToken(request);
        }
    }

    private void history() throws Exception {
        if (session.member == null) throw new UnauthorizedException();

        List<LoginEntity> logins = loginRepo.byPassword(session.member);
        send(new LoginHistoryResponse(new Headers(), logins));
    }

    private void loginByToken(LoginRequest request) throws Exception {
        Database database = session.server.serverConfig.database();
        Tokenizer tokenizer = session.server.serverConfig.tokenizer();

        String token = request.content();

        session.member = tokenizer
                .findMember(database, token)
                .orElseThrow(UnauthorizedException::new);


        String ip = session.io.socket.getInetAddress().getHostAddress();
        loginRepo.create(session.member, ip, false);

        onLogin();

        sendFin(new LoginResponse(session.member));
    }

    protected void onLogin() throws Exception {}
}
