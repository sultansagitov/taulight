package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.db.FileEntity;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.db.MemberRepository;
import net.result.sandnode.dto.FileDTO;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.ImpossibleRuntimeException;
import net.result.sandnode.exception.error.*;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.TextMessage;
import net.result.sandnode.message.UUIDMessage;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.util.DBFileUtil;
import net.result.sandnode.util.FileIOUtil;
import net.result.sandnode.util.JPAUtil;
import net.result.taulight.cluster.TauClusterManager;
import net.result.taulight.db.*;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.dto.CodeDTO;
import net.result.taulight.dto.GroupRequestDTO;
import net.result.taulight.dto.InviteCodeDTO;
import net.result.taulight.message.CodeListMessage;
import net.result.taulight.message.TauMessageTypes;
import net.result.taulight.message.types.GroupRequest;
import net.result.taulight.util.ChatUtil;
import net.result.taulight.util.ClusterUtil;
import net.result.taulight.util.SysMessages;
import net.result.taulight.util.TauHubProtocol;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class GroupServerChain extends ServerChain implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(GroupServerChain.class);
    private ChatUtil chatUtil;
    private DBFileUtil dbFileUtil;
    private TauClusterManager manager;
    private GroupRepository groupRepo;
    private MemberRepository memberRepo;
    private InviteCodeRepository inviteCodeRepo;

    public GroupServerChain(Session session) {
        super(session);
    }

    @Override
    public @Nullable Message handle(RawMessage raw) throws Exception {
        JPAUtil jpaUtil = session.server.container.get(JPAUtil.class);
        chatUtil = session.server.container.get(ChatUtil.class);
        dbFileUtil = session.server.container.get(DBFileUtil.class);

        manager = session.server.container.get(TauClusterManager.class);

        groupRepo = session.server.container.get(GroupRepository.class);
        memberRepo = session.server.container.get(MemberRepository.class);
        inviteCodeRepo = session.server.container.get(InviteCodeRepository.class);

        GroupRequest request = new GroupRequest(raw);

        GroupRequestDTO dto = request.dto();

        if (dto.type == null) {
            throw new TooFewArgumentsException();
        }

        if (session.member == null) {
            throw new UnauthorizedException();
        }

        final TauMemberEntity you = session.member.tauMember();

        Message response = switch (dto.type) {
            case CREATE -> create(dto, you);
            case INVITE -> invite(dto, you);
            case LEAVE -> leave(dto, you);
            case CH_CODES -> groupCodes(dto, you);
            case MY_CODES -> myCodes(you);
            case SET_AVATAR -> setAvatar(dto, you);
            case GET_AVATAR -> getAvatar(dto, you);
        };

        session.member = jpaUtil.refresh(session.member);

        return response;
    }

    private UUIDMessage create(GroupRequestDTO dto, TauMemberEntity you) throws Exception {
        if (dto.title == null) {
            throw new TooFewArgumentsException();
        }

        GroupEntity group = groupRepo.create(dto.title, you);

        ClusterUtil.addMemberToCluster(session, manager.getCluster(group));
        ChatMessageInputDTO input = SysMessages.groupNew.toInput(group, you);
        try {
            TauHubProtocol.send(session, group, input);
        } catch (UnauthorizedException e) {
            throw new ImpossibleRuntimeException(e);
        } catch (DatabaseException | NoEffectException e) {
            LOGGER.warn("Exception when sending system message of creating group {}", e.getMessage());
        }

        return new UUIDMessage(new Headers().setType(MessageTypes.HAPPY), group);
    }

    private TextMessage invite(GroupRequestDTO dto, TauMemberEntity you) throws Exception {
        ChatEntity chat = chatUtil.getChat(dto.chatID).orElseThrow(NotFoundException::new);

        // You not in group
        if (!chatUtil.contains(chat, you)) throw new NotFoundException();

        // This is not group
        if (!(chat instanceof GroupEntity group)) throw new WrongAddressException();

        TauMemberEntity member = memberRepo
                .findByNickname(dto.otherNickname)
                .map(MemberEntity::tauMember)
                .orElseThrow(AddressedMemberNotFoundException::new);

        // You are not owner
        //TODO add settings for inviting by another members
        if (!group.owner().equals(you)) throw new UnauthorizedException();

        // Receiver already in group
        if (chatUtil.contains(chat, member)) throw new NoEffectException();

        // Receiver already have invite code
        for (InviteCodeEntity inviteCodeEntity : inviteCodeRepo.find(group, member)) {
            if (inviteCodeEntity.activationDate() == null
                    || !inviteCodeEntity.expiresDate().isAfter(ZonedDateTime.now())) {
                throw new NoEffectException();
            }
        }

        String expirationString = dto.expirationTime;
        Duration duration = Duration.ofSeconds(Long.parseLong(expirationString));
        ZonedDateTime expiresDate = ZonedDateTime.now().plus(duration);
        InviteCodeEntity code = inviteCodeRepo.create(group, member, you, expiresDate);

        return new TextMessage(new Headers().setType(TauMessageTypes.GROUP), code.code());
    }

    private HappyMessage leave(GroupRequestDTO dto, TauMemberEntity you) throws Exception {
        ChatEntity chat = chatUtil.getChat(dto.chatID).orElseThrow(NotFoundException::new);

        // You not in group
        if (!chatUtil.contains(chat, you)) throw new NotFoundException();

        // This is not chanel
        if (!(chat instanceof GroupEntity group)) throw new WrongAddressException();

        // You cannot leave, because you are owner
        if (group.owner().equals(you)) throw new UnauthorizedException();

        // You are not in group (impossible)
        if (!groupRepo.removeMember(group, you)) throw new NotFoundException();

        ChatMessageInputDTO input = SysMessages.groupLeave.toInput(group, you);
        try {
            TauHubProtocol.send(session, group, input);
        } catch (UnauthorizedException e) {
            throw new ImpossibleRuntimeException(e);
        } catch (DatabaseException | NoEffectException e) {
            LOGGER.warn("Exception when sending system message of leaving member: {}", e.getMessage());
        }

        ClusterUtil.removeMemberFromCluster(session, manager.getCluster(group));

        return new HappyMessage();
    }

    private CodeListMessage groupCodes(GroupRequestDTO dto, TauMemberEntity you) throws Exception {
        ChatEntity chat = chatUtil.getChat(dto.chatID).orElseThrow(NotFoundException::new);
        if (!chatUtil.contains(chat, you)) throw new UnauthorizedException();
        if (!(chat instanceof GroupEntity group)) throw new WrongAddressException();

        Headers headers = new Headers().setType(TauMessageTypes.GROUP);

        Collection<CodeDTO> collected = group.inviteCodes().stream()
                .map(InviteCodeDTO::new)
                .collect(Collectors.toSet());

        return new CodeListMessage(headers, collected);
    }

    private CodeListMessage myCodes(@NotNull TauMemberEntity you) {
        Collection<CodeDTO> collected = you
                .inviteCodesAsReceiver().stream()
                .map(InviteCodeDTO::new)
                .collect(Collectors.toSet());

        return new CodeListMessage(new Headers().setType(TauMessageTypes.GROUP), collected);
    }

    private UUIDMessage setAvatar(GroupRequestDTO request, TauMemberEntity you) throws Exception {
        UUID chatID = request.chatID;

        ChatEntity chat = chatUtil.getChat(chatID).orElseThrow(NotFoundException::new);
        if (!chatUtil.contains(chat, you)) throw new UnauthorizedException();
        if (!(chat instanceof GroupEntity group)) throw new WrongAddressException();

        FileDTO dto = FileIOUtil.receive(this::receive);

        if (!dto.contentType().startsWith("image/")) {
            throw new InvalidArgumentException();
        }

        FileEntity avatar = dbFileUtil.saveFile(dto, chatID.toString());

        groupRepo.setAvatar(group, avatar);

        return new UUIDMessage(new Headers().setType(MessageTypes.HAPPY), avatar);
    }

    @SuppressWarnings("SameReturnValue")
    private Message getAvatar(GroupRequestDTO dto, TauMemberEntity you) throws Exception {
        UUID chatID = dto.chatID;

        ChatEntity chat = chatUtil.getChat(chatID).orElseThrow(NotFoundException::new);
        if (!chatUtil.contains(chat, you)) throw new UnauthorizedException();
        if (!(chat instanceof GroupEntity group)) throw new WrongAddressException();

        FileEntity avatar = group.avatar();
        if (avatar == null) throw new NoEffectException(); 

        FileIOUtil.send(dbFileUtil.readImage(avatar), this::send);
        return null;
    }
}
