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

import java.util.List;

public class LoginServerChain extends ServerChain implements ReceiverChain {
    private LoginRepository loginRepo;

    public LoginServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws Exception {
        loginRepo = session.server.container.get(LoginRepository.class);

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

        List<LoginEntity> logins = loginRepo.byDevice(session.member);
        send(new LoginHistoryResponse(new Headers(), logins));
    }

    private void loginByToken(LoginRequest request) throws Exception {
        Tokenizer tokenizer = session.server.container.get(Tokenizer.class);

        String token = request.content();

        LoginEntity login = tokenizer.findLogin(token).orElseThrow(UnauthorizedException::new);
        session.member = login.member();

        String ip = session.io.socket.getInetAddress().getHostAddress();
        loginRepo.create(login, ip);

        onLogin();

        sendFin(new LoginResponse(session.member));
    }

    protected void onLogin() throws Exception {}
}
