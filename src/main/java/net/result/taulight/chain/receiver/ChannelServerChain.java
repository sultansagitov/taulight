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
import net.result.sandnode.message.TextMessage;
import net.result.sandnode.message.types.FileMessage;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.util.DBFileUtil;
import net.result.taulight.util.ChatUtil;
import net.result.taulight.util.SysMessages;
import net.result.taulight.util.TauAgentProtocol;
import net.result.taulight.util.TauHubProtocol;
import net.result.taulight.db.*;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.dto.InviteCodeDTO;
import net.result.taulight.dto.CodeDTO;
import net.result.taulight.group.TauGroupManager;
import net.result.taulight.message.CodeListMessage;
import net.result.taulight.message.TauMessageTypes;
import net.result.taulight.message.types.ChannelRequest;
import net.result.sandnode.message.UUIDMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class ChannelServerChain extends ServerChain implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(ChannelServerChain.class);
    private ChatUtil chatUtil;
    private TauGroupManager manager;
    private ChannelRepository channelRepo;
    private MemberRepository memberRepo;
    private InviteCodeRepository inviteCodeRepo;
    private DBFileUtil dbFileUtil;

    public ChannelServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws Exception {
        chatUtil = session.server.container.get(ChatUtil.class);
        dbFileUtil = session.server.container.get(DBFileUtil.class);

        manager = session.server.container.get(TauGroupManager.class);

        channelRepo = session.server.container.get(ChannelRepository.class);
        memberRepo = session.server.container.get(MemberRepository.class);
        inviteCodeRepo = session.server.container.get(InviteCodeRepository.class);

        ChannelRequest request = new ChannelRequest(queue.take());

        if (request.type == null) {
            throw new TooFewArgumentsException();
        }

        if (session.member == null) {
            throw new UnauthorizedException();
        }

        final TauMemberEntity you = session.member.tauMember();
        switch (request.type) {
            case CREATE -> create(request, you);
            case INVITE -> invite(request, you);
            case LEAVE -> leave(request, you);
            case CH_CODES -> channelCodes(request, you);
            case MY_CODES -> myCodes(you);
            case SET_AVATAR -> setAvatar(request, you);
            case GET_AVATAR -> getAvatar(request, you);
        }
    }

    private void create(@NotNull ChannelRequest request, TauMemberEntity you) throws Exception {
        if (request.title == null) {
            throw new TooFewArgumentsException();
        }

        ChannelEntity channel = channelRepo.create(request.title, you);

        TauAgentProtocol.addMemberToGroup(session, manager.getGroup(channel));
        ChatMessageInputDTO input = SysMessages.channelNew.toInput(channel, you);
        try {
            TauHubProtocol.send(session, channel, input);
        } catch (UnauthorizedException e) {
            throw new ImpossibleRuntimeException(e);
        } catch (DatabaseException | NoEffectException e) {
            LOGGER.warn("Exception when sending system message of creating channel {}", e.getMessage());
        }

        send(new UUIDMessage(new Headers().setType(MessageTypes.HAPPY), channel));
    }

    private void invite(@NotNull ChannelRequest request, TauMemberEntity you) throws Exception {
        ChatEntity chat = chatUtil.getChat(request.chatID).orElseThrow(NotFoundException::new);

        Collection<TauMemberEntity> members = chatUtil.getMembers(chat);

        // You not in channel
        if (!members.contains(you)) throw new NotFoundException();

        // This is not channel
        if (!(chat instanceof ChannelEntity channel)) throw new WrongAddressException();

        TauMemberEntity member = memberRepo
                .findByNickname(request.otherNickname)
                .map(MemberEntity::tauMember)
                .orElseThrow(AddressedMemberNotFoundException::new);

        // You are not owner
        //TODO add settings for inviting by another members
        if (channel.owner() != you) throw new UnauthorizedException();

        // Receiver already in channel
        if (members.contains(member)) throw new NoEffectException();

        // Receiver already have invite code
        for (InviteCodeEntity inviteCodeEntity : inviteCodeRepo.find(channel, member)) {
            if (inviteCodeEntity.activationDate() == null
                    || !inviteCodeEntity.expiresDate().isAfter(ZonedDateTime.now())) {
                throw new NoEffectException();
            }
        }

        ZonedDateTime expiresDate = ZonedDateTime.now().plus(request.expirationTime);
        InviteCodeEntity code = inviteCodeRepo.create(channel, member, you, expiresDate);

        sendFin(new TextMessage(new Headers().setType(TauMessageTypes.CHANNEL), code.code()));
    }

    private void leave(@NotNull ChannelRequest request, TauMemberEntity you) throws Exception {
        ChatEntity chat = chatUtil.getChat(request.chatID).orElseThrow(NotFoundException::new);

        Collection<TauMemberEntity> members = chatUtil.getMembers(chat);

        // You not in channel
        if (!members.contains(you)) throw new NotFoundException();

        // This is not chanel
        if (!(chat instanceof ChannelEntity channel)) throw new WrongAddressException();

        // You cannot leave, because you are owner
        if (channel.owner() == you) throw new UnauthorizedException();

        // You are not in channel (impossible)
        if (!channelRepo.removeMember(channel, you)) throw new NotFoundException();

        ChatMessageInputDTO input = SysMessages.channelLeave.toInput(channel, you);
        try {
            TauHubProtocol.send(session, channel, input);
        } catch (UnauthorizedException e) {
            throw new ImpossibleRuntimeException(e);
        } catch (DatabaseException | NoEffectException e) {
            LOGGER.warn("Exception when sending system message of leaving member: {}", e.getMessage());
        }

        TauAgentProtocol.removeMemberFromGroup(session, manager.getGroup(channel));

        send(new HappyMessage());
    }

    private void channelCodes(@NotNull ChannelRequest request, TauMemberEntity you) throws Exception {
        ChatEntity chat = chatUtil.getChat(request.chatID).orElseThrow(NotFoundException::new);
        if (!chatUtil.getMembers(chat).contains(you)) throw new UnauthorizedException();
        if (!(chat instanceof ChannelEntity channel)) throw new WrongAddressException();

        Headers headers = new Headers().setType(TauMessageTypes.CHANNEL);

        Collection<CodeDTO> collected = channel.inviteCodes().stream()
                .map(InviteCodeDTO::new)
                .collect(Collectors.toSet());

        sendFin(new CodeListMessage(headers, collected));
    }

    private void myCodes(@NotNull TauMemberEntity you) throws Exception {
        Collection<CodeDTO> collected = you
                .inviteCodesAsReceiver().stream()
                .map(InviteCodeDTO::new)
                .collect(Collectors.toSet());

        sendFin(new CodeListMessage(new Headers().setType(TauMessageTypes.CHANNEL), collected));
    }

    private void setAvatar(@NotNull ChannelRequest request, TauMemberEntity you) throws Exception {
        UUID chatID = request.chatID;
        FileMessage fileMessage = new FileMessage(queue.take());

        ChatEntity chat = chatUtil.getChat(chatID).orElseThrow(NotFoundException::new);
        if (!chatUtil.getMembers(chat).contains(you)) throw new UnauthorizedException();
        if (!(chat instanceof ChannelEntity channel)) throw new WrongAddressException();

        FileDTO dto = fileMessage.dto();
        FileEntity avatar = dbFileUtil.saveImage(dto, chatID.toString());

        channelRepo.setAvatar(channel, avatar);

        send(new HappyMessage());
    }

    private void getAvatar(@NotNull ChannelRequest request, TauMemberEntity you) throws Exception {
        UUID chatID = request.chatID;

        ChatEntity chat = chatUtil.getChat(chatID).orElseThrow(NotFoundException::new);
        if (!chatUtil.getMembers(chat).contains(you)) throw new UnauthorizedException();
        if (!(chat instanceof ChannelEntity channel)) throw new WrongAddressException();

        FileEntity avatar = channel.avatar();
        if (avatar == null) throw new NoEffectException(); 
        
        byte[] bytes = dbFileUtil.readImage(avatar.filename());
        FileDTO fileDTO = new FileDTO(avatar.contentType(), bytes);
        send(new FileMessage(new Headers(), fileDTO));
    }
}
