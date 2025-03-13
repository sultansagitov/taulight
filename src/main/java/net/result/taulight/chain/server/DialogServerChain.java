package net.result.taulight.chain.server;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.db.Member;
import net.result.sandnode.error.Errors;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.SysMessages;
import net.result.taulight.TauAgentProtocol;
import net.result.taulight.TauHubProtocol;
import net.result.taulight.chain.client.DialogRequest;
import net.result.taulight.db.ChatMessage;
import net.result.taulight.db.TauDatabase;
import net.result.taulight.db.TauDialog;
import net.result.taulight.exception.error.MessageNotForwardedException;
import net.result.taulight.group.TauGroupManager;
import net.result.taulight.message.types.UUIDMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class DialogServerChain extends ServerChain implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(DialogServerChain.class);

    public DialogServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, ExpectedMessageException, UnprocessedMessagesException {
        TauDatabase database = (TauDatabase) session.server.serverConfig.database();
        TauGroupManager manager = (TauGroupManager) session.server.serverConfig.groupManager();

        DialogRequest request = new DialogRequest(queue.take());

        if (session.member == null) {
            sendFin(Errors.UNAUTHORIZED.createMessage());
            return;
        }

        TauDialog dialog;
        Optional<Member> anotherMember;
        try {
            anotherMember = database.findMemberByNickname(request.memberID());
            if (anotherMember.isEmpty()) {
                send(Errors.MEMBER_NOT_FOUND.createMessage());
                return;
            }

            Optional<TauDialog> dialogOpt = database.findDialog(session.member, anotherMember.get());
            if (dialogOpt.isPresent()) {
                dialog = dialogOpt.get();
            } else {
                dialog = database.createDialog(session.member, anotherMember.get());

                ChatMessage chatMessage = SysMessages.dialogNew.chatMessage(dialog, session.member);

                try {
                    TauHubProtocol.send(session.server.serverConfig, dialog, chatMessage);
                } catch (DatabaseException | MessageNotForwardedException e) {
                    LOGGER.warn("Ignored exception: {}", e.getMessage());
                }
            }
        } catch (DatabaseException e) {
            throw new RuntimeException(e);
        }

        Collection<Member> members = List.of(session.member, anotherMember.get());
        TauAgentProtocol.addMembersToGroup(session, members, manager.getGroup(dialog));

        sendFin(new UUIDMessage(new Headers().setType(MessageTypes.HAPPY), dialog));
    }
}
