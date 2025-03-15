package net.result.sandnode.chain.server;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.db.Database;
import net.result.sandnode.exception.SandnodeException;
import net.result.sandnode.exception.error.BusyNicknameException;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.RegistrationRequest;
import net.result.sandnode.message.types.RegistrationResponse;
import net.result.sandnode.error.Errors;
import net.result.sandnode.security.Tokenizer;
import net.result.sandnode.serverclient.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RegistrationServerChain extends ServerChain implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(RegistrationServerChain.class);

    public RegistrationServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, SandnodeException {
        RawMessage request = queue.take();

        Database database = session.server.serverConfig.database();
        Tokenizer tokenizer = session.server.serverConfig.tokenizer();

        RegistrationRequest regMsg = new RegistrationRequest(request);
        String nickname = regMsg.getNickname();
        String password = regMsg.getPassword();

        if (nickname.isEmpty() || password.isEmpty()) {
            sendFin(Errors.INVALID_NICKNAME_OR_PASSWORD.createMessage());
            return;
        }

        try {
            session.member = database.registerMember(nickname, password);
            String token = tokenizer.tokenizeMember(session.member);
            sendFin(new RegistrationResponse(token));
        } catch (BusyNicknameException e) {
            sendFin(Errors.BUSY_NICKNAME.createMessage());
        } catch (DatabaseException e) {
            LOGGER.error("Registering exception", e);
            sendFin(Errors.SERVER_ERROR.createMessage());
        }
    }
}
