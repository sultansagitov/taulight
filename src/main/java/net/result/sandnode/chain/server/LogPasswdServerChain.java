package net.result.sandnode.chain.server;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.db.Database;
import net.result.sandnode.db.Member;
import net.result.sandnode.error.Errors;
import net.result.sandnode.exception.*;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.*;
import net.result.sandnode.serverclient.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public abstract class LogPasswdServerChain extends ServerChain implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(LogPasswdServerChain.class);

    public LogPasswdServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, SandnodeException {
        RawMessage request = queue.take();
        LogPasswdRequest msg = new LogPasswdRequest(request);

        Database database = session.server.serverConfig.database();
        Optional<Member> opt;

        try {
            opt = database.findMemberByMemberID(msg.getMemberID());
        } catch (DatabaseException e) {
            LOGGER.error(e);
            sendFin(Errors.SERVER_ERROR.createMessage());
            return;
        }

        if (opt.isEmpty()) {
            sendFin(Errors.MEMBER_NOT_FOUND.createMessage());
            return;
        }

        boolean verified = database.hasher().verify(msg.getPassword(), opt.get().hashedPassword());
        if (!verified) {
            send(Errors.UNAUTHORIZED.createMessage());
            return;
        }

        session.member = opt.get();

        onLogin();

        String token = session.server.serverConfig.tokenizer().tokenizeMember(session.member);
        sendFin(new LogPasswdResponse(token));
    }

    protected abstract void onLogin() throws InterruptedException, SandnodeException;
}
