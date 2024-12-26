package net.result.sandnode.chain.server;

import net.result.sandnode.config.IServerConfig;
import net.result.sandnode.exceptions.ExpectedMessageException;
import net.result.sandnode.exceptions.MemberNotFound;
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

public class AuthServerChain extends ServerChain {
    private static final Logger LOGGER = LogManager.getLogger(AuthServerChain.class);

    public AuthServerChain(Session session) {
        super(session);
    }

    @Override
    public void start() throws InterruptedException, ExpectedMessageException, MemberNotFound {
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

                RegistrationResponse response = new RegistrationResponse(new Headers(), token);
                send(response);
                return;
            }
            case LOGIN -> {
                TokenMessage msg = new TokenMessage(request);
                String token = msg.getToken();

                IDatabase database = session.server.serverConfig.database();
                Optional<IMember> opt = session.server.serverConfig.tokenizer().findMember(database, token);

                if (opt.isEmpty()) throw new MemberNotFound();

                session.member = opt.get();

                IMessage response = new LoginResponse(new Headers(), session.member);
                send(response);
            }
        }
        LOGGER.error("not auth request");
    }
}
