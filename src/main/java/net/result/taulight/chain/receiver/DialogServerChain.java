package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.receiver.ServerChain;
import net.result.sandnode.db.Member;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.ImpossibleRuntimeException;
import net.result.sandnode.exception.error.AddressedMemberNotFoundException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.SysMessages;
import net.result.taulight.TauAgentProtocol;
import net.result.taulight.TauHubProtocol;
import net.result.taulight.chain.sender.DialogRequest;
import net.result.taulight.dto.ChatMessage;
import net.result.taulight.db.TauDatabase;
import net.result.taulight.db.TauDialog;
import net.result.sandnode.exception.error.NoEffectException;
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
    public void sync() throws Exception {
        TauDatabase database = (TauDatabase) session.server.serverConfig.database();
        TauGroupManager manager = (TauGroupManager) session.server.serverConfig.groupManager();

        DialogRequest request = new DialogRequest(queue.take());

        if (session.member == null) {
            throw new UnauthorizedException();
        }

        TauDialog dialog;
        Member anotherMember = database
                .findMemberByNickname(request.nickname())
                .orElseThrow(AddressedMemberNotFoundException::new);

        Optional<TauDialog> dialogOpt = database.findDialog(session.member, anotherMember);
        if (dialogOpt.isPresent()) {
            dialog = dialogOpt.get();
        } else {
            dialog = database.createDialog(session.member, anotherMember);

            ChatMessage chatMessage = SysMessages.dialogNew.chatMessage(dialog, session.member);

            try {
                TauHubProtocol.send(session, dialog, chatMessage);
            } catch (UnauthorizedException e) {
                throw new ImpossibleRuntimeException(e);
            } catch (DatabaseException | NoEffectException e) {
                LOGGER.warn("Ignored exception: {}", e.getMessage());
            }
        }

        Collection<Member> members = List.of(session.member, anotherMember);
        TauAgentProtocol.addMembersToGroup(session, members, manager.getGroup(dialog));

        sendFin(new UUIDMessage(new Headers().setType(MessageTypes.HAPPY), dialog));
    }
}
