package net.result.sandnode.chain.server;

import net.result.sandnode.config.IServerConfig;
import net.result.sandnode.exceptions.BusyMemberIDException;
import net.result.sandnode.exceptions.DeserializationException;
import net.result.sandnode.exceptions.ExpectedMessageException;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.types.RegistrationRequest;
import net.result.sandnode.messages.types.RegistrationResponse;
import net.result.sandnode.server.Session;

import static net.result.sandnode.server.ServerError.INVALID_MEMBER_ID_OR_PASSWORD;
import static net.result.sandnode.server.ServerError.MEMBER_ID_BUSY;

public class RegistrationServerChain extends ServerChain {
    public RegistrationServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, ExpectedMessageException, DeserializationException {
        IMessage request = queue.take();
        RegistrationRequest regMsg = new RegistrationRequest(request);
        IServerConfig serverConfig = session.server.serverConfig;
        String memberID = regMsg.getMemberID();
        String password = regMsg.getPassword();

        if (memberID.isEmpty() || password.isEmpty()) {
            sendFin(INVALID_MEMBER_ID_OR_PASSWORD.message());
            return;
        }

        try {
            session.member = serverConfig.database().registerMember(memberID, password);
            String token = serverConfig.tokenizer().tokenizeMember(session.member);
            sendFin(new RegistrationResponse(token));
        } catch (BusyMemberIDException e) {
            sendFin(MEMBER_ID_BUSY.message());
        }
    }
}
