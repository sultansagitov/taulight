package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.receiver.ServerChain;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.ImpossibleRuntimeException;
import net.result.sandnode.exception.error.*;
import net.result.sandnode.message.TextMessage;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.SysMessages;
import net.result.taulight.TauAgentProtocol;
import net.result.taulight.TauHubProtocol;
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
import java.util.stream.Collectors;

public class ChannelServerChain extends ServerChain implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(ChannelServerChain.class);

    public ChannelServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws Exception {
        ChannelRequest request = new ChannelRequest(queue.take());

        if (request.type == null) {
            throw new TooFewArgumentsException();
        }

        if (session.member == null) {
            throw new UnauthorizedException();
        }

        switch (request.type) {
            case CREATE -> create(request);
            case INVITE -> invite(request);
            case LEAVE -> leave(request);
            case CH_CODES -> channelCodes(request);
            case MY_CODES -> myCodes();
        }
    }

    private void create(@NotNull ChannelRequest request) throws Exception {
        var database = (TauDatabase) session.server.serverConfig.database();
        var manager = (TauGroupManager) session.server.serverConfig.groupManager();

        if (request.title == null) {
            throw new TooFewArgumentsException();
        }

        MemberEntity owner = session.member;
        ChannelEntity channel = database.createChannel(request.title, owner);

        if (!database.addMemberToChannel(channel, owner)) {
            throw new NoEffectException();
        }

        TauAgentProtocol.addMemberToGroup(session, manager.getGroup(channel));
        ChatMessageInputDTO input = SysMessages.channelNew.chatMessageInputDTO(channel, owner);
        try {
            TauHubProtocol.send(session, channel, input);
        } catch (UnauthorizedException e) {
            throw new ImpossibleRuntimeException(e);
        } catch (DatabaseException | NoEffectException e) {
            LOGGER.warn("Exception when sending system message of creating channel {}", e.getMessage());
        }

        send(new UUIDMessage(new Headers().setType(MessageTypes.HAPPY), channel));
    }

    private void invite(@NotNull ChannelRequest request) throws Exception {
        TauDatabase database = (TauDatabase) session.server.serverConfig.database();

        ChatEntity chat = database.getChat(request.chatID).orElseThrow(NotFoundException::new);

        Collection<MemberEntity> members = database.getMembers(chat);

        // You not in channel
        if (!members.contains(session.member)) {
            throw new NotFoundException();
        }

        if (!(chat instanceof ChannelEntity channel)) {
            throw new WrongAddressException();
        }

        MemberEntity member = database
                .findMemberByNickname(request.otherNickname)
                .orElseThrow(AddressedMemberNotFoundException::new);

        // You are not owner
        //TODO add settings for inviting by another members
        if (channel.owner() != session.member) {
            throw new UnauthorizedException();
        }

        // Receiver already in channel
        if (members.contains(member)) {
            throw new NoEffectException();
        }

        // Receiver already have invite code
        if (database.findInviteCode(channel, member).stream().anyMatch(code -> code.activationDate() == null)) {
            throw new NoEffectException();
        }

        ZonedDateTime expiresDate = ZonedDateTime.now().plus(request.expirationTime);
        InviteCodeEntity code = database.createInviteCode(channel, member, session.member, expiresDate);

        sendFin(new TextMessage(new Headers().setType(TauMessageTypes.CHANNEL), code.code()));
    }

    private void leave(@NotNull ChannelRequest request) throws Exception {
        TauDatabase database = (TauDatabase) session.server.serverConfig.database();
        TauGroupManager manager = (TauGroupManager) session.server.serverConfig.groupManager();

        ChatEntity chat = database.getChat(request.chatID).orElseThrow(NotFoundException::new);

        if (!(chat instanceof ChannelEntity channel)) {
            throw new WrongAddressException();
        }

        if (channel.owner() == session.member) {
            throw new UnauthorizedException();
        }

        if (!database.leaveFromChannel(channel, session.member)) {
            throw new NotFoundException();
        }

        ChatMessageInputDTO input = SysMessages.channelLeave.chatMessageInputDTO(channel, session.member);
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

    private void channelCodes(@NotNull ChannelRequest request) throws Exception {
        TauDatabase database = (TauDatabase) session.server.serverConfig.database();

        ChatEntity chat = database.getChat(request.chatID).orElseThrow(NotFoundException::new);

        if (!(chat instanceof ChannelEntity channel)) {
            throw new WrongAddressException();
        }

        Headers headers = new Headers().setType(TauMessageTypes.CHANNEL);

        Collection<CodeDTO> collected = channel
                .inviteCodes().stream()
                .map(InviteCodeDTO::new)
                .collect(Collectors.toSet());

        sendFin(new CodeListMessage(headers, collected));
    }

    @SuppressWarnings("DataFlowIssue")
    private void myCodes() throws Exception {
        Collection<CodeDTO> collected = session.member
                .inviteCodes().stream()
                .map(InviteCodeDTO::new)
                .collect(Collectors.toSet());

        sendFin(new CodeListMessage(new Headers().setType(TauMessageTypes.CHANNEL), collected));
    }
}
