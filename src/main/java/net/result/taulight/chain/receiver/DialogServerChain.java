package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.db.FileEntity;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.exception.ImpossibleRuntimeException;
import net.result.sandnode.exception.error.*;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.UUIDMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.util.DBFileUtil;
import net.result.sandnode.util.FileIOUtil;
import net.result.taulight.cluster.TauClusterManager;
import net.result.taulight.db.*;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.message.types.DialogRequest;
import net.result.taulight.util.ChatUtil;
import net.result.taulight.util.ClusterUtil;
import net.result.taulight.util.SysMessages;
import net.result.taulight.util.TauHubProtocol;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class DialogServerChain extends ServerChain implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(DialogServerChain.class);

    public DialogServerChain(Session session) {
        super(session);
    }

    @Override
    public Message handle(RawMessage raw) throws Exception {
        DialogRequest request = new DialogRequest(raw);

        if (session.member == null) {
            throw new UnauthorizedException();
        }

        DialogRequest.Type type = request.headers()
                .getOptionalValue("type")
                .map(name -> DialogRequest.Type.valueOf(name.toUpperCase()))
                .orElse(DialogRequest.Type.ID);

        switch (type) {
            case ID -> id(request, session.member);
            case AVATAR -> avatar(request, session.member);
        }

        return null;
    }

    private void id(DialogRequest request, MemberEntity you) throws Exception {
        TauClusterManager manager = session.server.container.get(TauClusterManager.class);
        TauMemberRepository tauMemberRepo = session.server.container.get(TauMemberRepository.class);
        DialogRepository dialogRepo = session.server.container.get(DialogRepository.class);

        TauMemberEntity anotherMember = tauMemberRepo
                .findByNickname(request.content())
                .orElseThrow(AddressedMemberNotFoundException::new);

        Optional<DialogEntity> dialogOpt = dialogRepo.findByMembers(you.tauMember(), anotherMember);

        DialogEntity dialog = dialogOpt.isPresent()
                ? dialogOpt.get()
                : dialogRepo.create(you.tauMember(), anotherMember);
        sendFin(new UUIDMessage(new Headers().setType(MessageTypes.HAPPY), dialog));

        if (dialogOpt.isEmpty()) {
            Collection<MemberEntity> members = new ArrayList<>(List.of(you, anotherMember.member()));
            ClusterUtil.addMembersToCluster(session, members, manager.getCluster(dialog));

            ChatMessageInputDTO input = SysMessages.dialogNew.toInput(dialog, you.tauMember());

            try {
                TauHubProtocol.send(session, dialog, input);
            } catch (UnauthorizedException e) {
                throw new ImpossibleRuntimeException(e);
            } catch (Exception e) {
                LOGGER.warn("Ignored exception: {}", e.getMessage());
            }
        }
    }

    private void avatar(DialogRequest request, MemberEntity you) throws Exception {
        ChatUtil chatUtil = session.server.container.get(ChatUtil.class);
        DBFileUtil dbFileUtil = session.server.container.get(DBFileUtil.class);

        UUID chatID = UUID.fromString(request.content());

        ChatEntity chat = chatUtil.getChat(chatID).orElseThrow(NotFoundException::new);
        if (!chatUtil.contains(chat, you.tauMember())) throw new UnauthorizedException();
        if (!(chat instanceof DialogEntity dialog)) throw new WrongAddressException();

        FileEntity avatar = dialog.otherMember(you.tauMember()).member().avatar();
        if (avatar == null) throw new NoEffectException();

        FileIOUtil.send(dbFileUtil.readImage(avatar), this::send);
    }
}
