package net.result.taulight.chain.server;

import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.db.Member;
import net.result.sandnode.error.Errors;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.SysMessages;
import net.result.taulight.TauAgentProtocol;
import net.result.taulight.TauHubProtocol;
import net.result.taulight.chain.client.DirectRequest;
import net.result.taulight.db.ChatMessage;
import net.result.taulight.db.TauDatabase;
import net.result.taulight.db.TauDirect;
import net.result.taulight.exception.MessageNotForwardedException;
import net.result.taulight.group.TauGroupManager;
import net.result.taulight.message.types.DirectResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class DirectServerChain extends ServerChain {
    private static final Logger LOGGER = LogManager.getLogger(DirectServerChain.class);

    public DirectServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, ExpectedMessageException, DeserializationException {
        TauDatabase database = (TauDatabase) session.server.serverConfig.database();
        TauGroupManager manager = (TauGroupManager) session.server.serverConfig.groupManager();

        DirectRequest request = new DirectRequest(queue.take());

        TauDirect direct;
        Optional<Member> anotherMember;
        try {
            anotherMember = database.findMemberByMemberID(request.getMemberID());
            if (anotherMember.isEmpty()) {
                send(Errors.MEMBER_NOT_FOUND.message());
                return;
            }

            Optional<TauDirect> directOpt = database.findDirectChat(session.member, anotherMember.get());
            if (directOpt.isPresent()) {
                direct = directOpt.get();
            } else {
                direct = database.createDirectChat(session.member, anotherMember.get());

                ChatMessage chatMessage = SysMessages.directNew.chatMessage(direct, session.member);

                try {
                    TauHubProtocol.send(session.server.serverConfig, direct, chatMessage);
                } catch (DatabaseException | MessageNotForwardedException e) {
                    LOGGER.warn("Ignored exception: {}", e.getMessage());
                }
            }
        } catch (DatabaseException e) {
            throw new RuntimeException(e);
        }

        Collection<Member> members = List.of(session.member, anotherMember.get());
        TauAgentProtocol.addMembersToGroup(session, members, manager.getGroup(direct));

        sendFin(new DirectResponse(request.getMemberID(), direct.getID()));
    }
}
