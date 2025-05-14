package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.db.FileEntity;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.db.MemberRepository;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.ImpossibleRuntimeException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.error.*;
import net.result.sandnode.message.types.FileMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.util.DBFileUtil;
import net.result.taulight.db.*;
import net.result.taulight.exception.AlreadyExistingRecordException;
import net.result.taulight.util.ChatUtil;
import net.result.taulight.util.SysMessages;
import net.result.taulight.util.TauAgentProtocol;
import net.result.taulight.util.TauHubProtocol;
import net.result.taulight.message.types.DialogRequest;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.group.TauGroupManager;
import net.result.sandnode.message.UUIDMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class DialogServerChain extends ServerChain implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(DialogServerChain.class);

    public DialogServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws Exception {
        DialogRequest request = new DialogRequest(queue.take());

        if (session.member == null) {
            throw new UnauthorizedException();
        }

        DialogRequest.Type type = request.headers()
                .getOptionalValue("type")
                .map(DialogRequest.Type::fromValue)
                .orElse(DialogRequest.Type.ID);

        switch (type) {
            case ID -> id(request, session.member);
            case AVATAR -> avatar(request, session.member);
        }
    }

    private void id(DialogRequest request, MemberEntity you)
            throws AddressedMemberNotFoundException, DatabaseException, AlreadyExistingRecordException,
            InterruptedException, UnprocessedMessagesException, NotFoundException {
        TauGroupManager manager = session.server.container.get(TauGroupManager.class);
        MemberRepository memberRepo = session.server.container.get(MemberRepository.class);
        DialogRepository dialogRepo = session.server.container.get(DialogRepository.class);

        DialogEntity dialog;
        TauMemberEntity anotherMember = memberRepo
                .findByNickname(request.content())
                .map(MemberEntity::tauMember)
                .orElseThrow(AddressedMemberNotFoundException::new);

        Optional<DialogEntity> dialogOpt = dialogRepo.findByMembers(you.tauMember(), anotherMember);
        if (dialogOpt.isPresent()) {
            dialog = dialogOpt.get();
        } else {
            dialog = dialogRepo.create(you.tauMember(), anotherMember);

            ChatMessageInputDTO input = SysMessages.dialogNew.toInput(dialog, you.tauMember());

            try {
                TauHubProtocol.send(session, dialog, input);
            } catch (UnauthorizedException e) {
                throw new ImpossibleRuntimeException(e);
            } catch (DatabaseException | NoEffectException e) {
                LOGGER.warn("Ignored exception: {}", e.getMessage());
            }
        }

        Collection<MemberEntity> members = new ArrayList<>(List.of(you, anotherMember.member()));
        TauAgentProtocol.addMembersToGroup(session, members, manager.getGroup(dialog));

        sendFin(new UUIDMessage(new Headers().setType(MessageTypes.HAPPY), dialog));
    }

    private void avatar(DialogRequest request, MemberEntity you) throws DatabaseException, UnauthorizedException,
            WrongAddressException, NotFoundException, NoEffectException, ServerSandnodeErrorException,
            UnprocessedMessagesException, InterruptedException {
        ChatUtil chatUtil = session.server.container.get(ChatUtil.class);
        DBFileUtil dbFileUtil = session.server.container.get(DBFileUtil.class);

        UUID chatID = UUID.fromString(request.content());

        ChatEntity chat = chatUtil.getChat(chatID).orElseThrow(NotFoundException::new);
        if (!chatUtil.getMembers(chat).contains(you.tauMember())) throw new UnauthorizedException();
        if (!(chat instanceof DialogEntity dialog)) throw new WrongAddressException();

        FileEntity avatar = dialog.otherMember(you.tauMember()).member().avatar();
        if (avatar == null) throw new NoEffectException();

        sendFin(new FileMessage(dbFileUtil.readImage(avatar)));
    }
}
