package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.receiver.ServerChain;
import net.result.sandnode.db.Member;
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
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.dto.InviteTauCode;
import net.result.taulight.dto.TauCode;
import net.result.taulight.db.*;
import net.result.taulight.exception.AlreadyExistingRecordException;
import net.result.taulight.group.TauGroupManager;
import net.result.taulight.message.CodeListMessage;
import net.result.taulight.message.TauMessageTypes;
import net.result.taulight.message.types.ChannelRequest;
import net.result.taulight.message.types.UUIDMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("DataFlowIssue")
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

        var channel = new TauChannel(database, request.title, session.member);
        while (true) {
            channel.setRandomID();
            try {
                channel.save();
                break;
            } catch (AlreadyExistingRecordException ignored) {
            }
        }

        channel.addMember(session.member);
        TauAgentProtocol.addMemberToGroup(session, manager.getGroup(channel));
        ChatMessageInputDTO chatMessage = SysMessages.channelNew.chatMessage(channel, session.member);
        try {
            TauHubProtocol.send(session, channel, chatMessage);
        } catch (UnauthorizedException e) {
            throw new ImpossibleRuntimeException(e);
        } catch (DatabaseException | NoEffectException e) {
            LOGGER.warn("Exception when sending system message of creating channel {}", e.getMessage());
        }

        send(new UUIDMessage(new Headers().setType(MessageTypes.HAPPY), channel));
    }

    private void invite(@NotNull ChannelRequest request) throws Exception {
        TauDatabase database = (TauDatabase) session.member.database();

        TauChat chat = database.getChat(request.chatID).orElseThrow(NotFoundException::new);

        if (!chat.getMembers().contains(session.member)) {
            throw new NotFoundException();
        }

        if (!(chat instanceof TauChannel channel)) {
            throw new WrongAddressException();
        }

        Member member = database
                .findMemberByNickname(request.otherNickname)
                .orElseThrow(AddressedMemberNotFoundException::new);

        //TODO add settings for inviting by another members
        if (!channel.owner().equals(session.member)) {
            throw new UnauthorizedException();
        }

        if (channel.getMembers().contains(member)) {
            throw new NoEffectException();
        }

        for (var code : channel.getActiveInviteCodes()) {
            if (code.getNickname().equals(member.nickname()) && code.getActivationDate() == null) {
                throw new NoEffectException();
            }
        }

        ZonedDateTime expiresDate = ZonedDateTime.now().plus(request.expirationTime);
        var token = new InviteCodeObject(database, channel, member, session.member, expiresDate);
        token.save();

        sendFin(new TextMessage(new Headers().setType(TauMessageTypes.CHANNEL), token.getCode()));
    }

    private void leave(@NotNull ChannelRequest request) throws Exception {
        TauDatabase database = (TauDatabase) session.server.serverConfig.database();
        TauGroupManager manager = (TauGroupManager) session.server.serverConfig.groupManager();

        TauChat tauChat = database.getChat(request.chatID).orElseThrow(NotFoundException::new);

        if (!(tauChat instanceof TauChannel channel)) {
            throw new WrongAddressException();
        }

        if (channel.owner().equals(session.member)) {
            throw new UnauthorizedException();
        }

        database.leaveFromChat(channel, session.member);
        ChatMessageInputDTO chatMessage = SysMessages.channelLeave.chatMessage(channel, session.member);
        try {
            TauHubProtocol.send(session, channel, chatMessage);
        } catch (UnauthorizedException e) {
            throw new ImpossibleRuntimeException(e);
        } catch (DatabaseException | NoEffectException e) {
            LOGGER.warn("Exception when sending system message of leaving member: {}", e.getMessage());
        }

        TauAgentProtocol.removeMemberFromGroup(session, manager.getGroup(channel));

        send(new HappyMessage());
    }

    private void channelCodes(@NotNull ChannelRequest request) throws Exception {
        TauDatabase database = (TauDatabase) session.member.database();

        TauChat chat = database.getChat(request.chatID).orElseThrow(NotFoundException::new);

        if (!(chat instanceof TauChannel channel)) {
            throw new WrongAddressException();
        }

        Collection<TauCode> collected = channel.getActiveInviteCodes().stream()
                .map(c -> new InviteTauCode(c, channel.title(), c.getNickname(), c.getSenderNickname()))
                .collect(Collectors.toSet());
        sendFin(new CodeListMessage(new Headers().setType(TauMessageTypes.CHANNEL), collected));
    }

    private void myCodes() throws Exception {
        TauDatabase database = (TauDatabase) session.member.database();

        Collection<TauCode> collected = new HashSet<>();
        for (InviteCodeObject c : database.getInviteCodesByNickname(session.member)) {
            UUID chatID = c.getChatID();
            TauChat chat = database.getChat(chatID).orElseThrow(NotFoundException::new);

            if (!(chat instanceof TauChannel channel)) {
                throw new WrongAddressException();
            }

            collected.add(new InviteTauCode(c, channel.title(), c.getNickname(), c.getSenderNickname()));
        }

        sendFin(new CodeListMessage(new Headers().setType(TauMessageTypes.CHANNEL), collected));
    }
}
