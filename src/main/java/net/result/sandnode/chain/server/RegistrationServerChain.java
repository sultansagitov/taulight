package net.result.sandnode.chain.server;

import net.result.sandnode.config.IServerConfig;
import net.result.sandnode.exception.BusyMemberIDException;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.types.RegistrationRequest;
import net.result.sandnode.message.types.RegistrationResponse;
import net.result.sandnode.error.Errors;
import net.result.sandnode.serverclient.Session;

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
            sendFin(Errors.INVALID_MEMBER_ID_OR_PASSWORD.message());
            return;
        }

        try {
            session.member = serverConfig.database().registerMember(memberID, password);
            String token = serverConfig.tokenizer().tokenizeMember(session.member);
            sendFin(new RegistrationResponse(token));
        } catch (BusyMemberIDException e) {
            sendFin(Errors.MEMBER_ID_BUSY.message());
        }
    }
}
