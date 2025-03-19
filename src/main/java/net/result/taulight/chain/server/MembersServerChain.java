package net.result.taulight.chain.server;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.db.Member;
import net.result.sandnode.error.Errors;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.db.TauChat;
import net.result.taulight.db.TauDatabase;
import net.result.taulight.message.types.MembersResponse;
import net.result.taulight.message.types.UUIDMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Optional;

public class MembersServerChain extends ServerChain implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(MembersServerChain.class);

    public MembersServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, DeserializationException, UnprocessedMessagesException {
        TauDatabase database = (TauDatabase) session.server.serverConfig.database();
        while (true) {
            UUIDMessage request = new UUIDMessage(queue.take());

            if (session.member == null) {
                send(Errors.UNAUTHORIZED.createMessage());
                continue;
            }

            try {
                Optional<TauChat> optChat = database.getChat(request.uuid);

                if (optChat.isEmpty()) {
                    send(Errors.NOT_FOUND.createMessage());
                    continue;
                }

                TauChat chat = optChat.get();
                Collection<Member> members = chat.getMembers();

                if (!members.contains(session.member)) {
                    send(Errors.NOT_FOUND.createMessage());
                    continue;
                }

                send(new MembersResponse(members));
            } catch (DatabaseException e) {
                LOGGER.error(e);
                send(Errors.SERVER_ERROR.createMessage());
                continue;
            }

            if (request.headers().fin()) break;
        }
    }
}
