package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.db.MemberRepository;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.ImpossibleRuntimeException;
import net.result.sandnode.exception.error.AddressedMemberNotFoundException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.util.SysMessages;
import net.result.taulight.util.TauAgentProtocol;
import net.result.taulight.util.TauHubProtocol;
import net.result.taulight.message.types.DialogRequest;
import net.result.taulight.db.TauMemberEntity;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.db.TauDatabase;
import net.result.taulight.db.DialogEntity;
import net.result.sandnode.exception.error.NoEffectException;
import net.result.taulight.group.TauGroupManager;
import net.result.sandnode.message.UUIDMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
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
        MemberRepository memberRepo = session.server.container.get(MemberRepository.class);

        DialogRequest request = new DialogRequest(queue.take());

        if (session.member == null) {
            throw new UnauthorizedException();
        }

        DialogEntity dialog;
        TauMemberEntity anotherMember = memberRepo
                .findByNickname(request.nickname())
                .map(MemberEntity::tauMember)
                .orElseThrow(AddressedMemberNotFoundException::new);

        Optional<DialogEntity> dialogOpt = database.findDialog(session.member.tauMember(), anotherMember);
        if (dialogOpt.isPresent()) {
            dialog = dialogOpt.get();
        } else {
            dialog = database.createDialog(session.member.tauMember(), anotherMember);

            ChatMessageInputDTO input = SysMessages.dialogNew.toInput(dialog, session.member.tauMember());

            try {
                TauHubProtocol.send(session, dialog, input);
            } catch (UnauthorizedException e) {
                throw new ImpossibleRuntimeException(e);
            } catch (DatabaseException | NoEffectException e) {
                LOGGER.warn("Ignored exception: {}", e.getMessage());
            }
        }

        Collection<MemberEntity> members = new ArrayList<>(List.of(session.member, anotherMember.member()));
        TauAgentProtocol.addMembersToGroup(session, members, manager.getGroup(dialog));

        sendFin(new UUIDMessage(new Headers().setType(MessageTypes.HAPPY), dialog));
    }
}
