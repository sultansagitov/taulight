package net.result.sandnode.chain.server;

import net.result.sandnode.config.IServerConfig;
import net.result.sandnode.exceptions.ExpectedMessageException;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.types.LoginResponse;
import net.result.sandnode.messages.types.RegistrationRequest;
import net.result.sandnode.messages.types.RegistrationResponse;
import net.result.sandnode.messages.types.TokenMessage;
import net.result.sandnode.messages.util.Headers;
import net.result.sandnode.messages.util.IMessageType;
import net.result.sandnode.messages.util.MessageType;
import net.result.sandnode.server.Session;
import net.result.sandnode.util.db.IDatabase;
import net.result.sandnode.util.db.IMember;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

import static net.result.sandnode.server.ServerError.MEMBER_NOT_FOUND;

public class AuthServerChain extends ServerChain {
    private static final Logger LOGGER = LogManager.getLogger(AuthServerChain.class);

    public AuthServerChain(Session session) {
        super(session);
    }

    @Override
    public void start() throws InterruptedException, ExpectedMessageException {
        IMessage request = queue.take();
        IMessageType type = request.getHeaders().getType();
        if (!(type instanceof MessageType systemType)) {
            LOGGER.error("Not a sandnode system type");
            return;
        }
        switch (systemType) {
            case REG -> {
                RegistrationRequest regMsg = new RegistrationRequest(request);
                IServerConfig serverConfig = session.server.serverConfig;
                session.member = serverConfig.database().registerMember(regMsg.getMemberID(), regMsg.getPassword());
                String token = serverConfig.tokenizer().tokenizeMember(session.member);

                sendFin(new RegistrationResponse(new Headers(), token));
            }
            case LOGIN -> {
                TokenMessage msg = new TokenMessage(request);
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
            default -> LOGGER.error("not auth request");
        }
    }
}
