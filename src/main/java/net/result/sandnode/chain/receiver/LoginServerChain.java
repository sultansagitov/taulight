package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.db.LoginEntity;
import net.result.sandnode.db.LoginRepository;
import net.result.sandnode.dto.LoginHistoryDTO;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.LoginHistoryResponse;
import net.result.sandnode.message.types.LoginRequest;
import net.result.sandnode.message.types.LoginResponse;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.security.Tokenizer;
import net.result.sandnode.serverclient.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LoginServerChain extends ServerChain implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(LoginServerChain.class);
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
        if (session.member == null) {
            throw new UnauthorizedException();
        }

        List<LoginEntity> logins = loginRepo.byDevice(session.member);

        Set<LoginEntity> onlineLogins = session.server.node.getAgents().stream()
                .map(agent -> agent.login)
                .collect(Collectors.toSet());

        LOGGER.debug(onlineLogins);

        List<LoginHistoryDTO> list = new ArrayList<>();
        for (LoginEntity login : logins) {
            boolean isOnline = onlineLogins.contains(login) ||
                    (login.login() != null && onlineLogins.contains(login.login()));
            LoginHistoryDTO loginHistoryDTO = new LoginHistoryDTO(login, isOnline);
            list.add(loginHistoryDTO);
        }

        send(new LoginHistoryResponse(new Headers(), list));
    }

    private void loginByToken(LoginRequest request) throws Exception {
        Tokenizer tokenizer = session.server.container.get(Tokenizer.class);

        String token = request.content();

        LoginEntity login = tokenizer.findLogin(token).orElseThrow(UnauthorizedException::new);
        session.member = login.member();
        session.login = login;

        String ip = session.io.socket.getInetAddress().getHostAddress();
        loginRepo.create(login, ip);

        onLogin();

        sendFin(new LoginResponse(session.member));
    }

    protected void onLogin() throws Exception {}
}
