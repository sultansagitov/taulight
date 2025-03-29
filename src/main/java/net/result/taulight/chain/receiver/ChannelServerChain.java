package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.receiver.ServerChain;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.*;
import net.result.sandnode.message.TextMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import net.result.taulight.code.InviteTauCode;
import net.result.taulight.code.TauCode;
import net.result.taulight.db.*;
import net.result.taulight.exception.AlreadyExistingRecordException;
import net.result.taulight.SysMessages;
import net.result.taulight.TauAgentProtocol;
import net.result.taulight.TauHubProtocol;
import net.result.sandnode.db.Member;
import net.result.taulight.group.TauGroupManager;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.message.CodeListMessage;
import net.result.taulight.message.TauMessageTypes;
import net.result.taulight.message.types.ChannelRequest;
import net.result.taulight.message.types.UUIDMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.util.*;
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

        switch (request.type) {
            case NEW -> NEW(request);
            case ADD -> ADD(request);
            case LEAVE -> LEAVE(request);
            case CODES -> CODES(request);
            case MY_INVITES -> MY_INVITES(request);
        }
    }

    private void NEW(@NotNull ChannelRequest request) throws Exception {
        TauDatabase database = (TauDatabase) session.server.serverConfig.database();
        TauGroupManager manager = (TauGroupManager) session.server.serverConfig.groupManager();

        String title = request.title;

        if (title == null) {
            throw new TooFewArgumentsException();
        }

        TauChannel channel = new TauChannel(database, title, session.member);
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

        ChatMessage chatMessage = SysMessages.channelNew.chatMessage(channel, session.member);

        try {
            TauHubProtocol.send(session, channel, chatMessage);
        } catch (UnauthorizedException e) {
            throw new ImpossibleRuntimeException(e);
        } catch (DatabaseException | NoEffectException e) {
            LOGGER.warn("Exception when sending system message of creating channel {}", e.getMessage());
        }

        send(new UUIDMessage(new Headers().setType(MessageTypes.HAPPY), channel));
    }

    private void ADD(@NotNull ChannelRequest request) throws Exception {
        if (session.member == null) {
            throw new UnauthorizedException();
        }

        TauDatabase database = (TauDatabase) session.member.database();

        UUID chatID = request.chatID;
        String otherNickname = request.otherNickname;

        TauChat chat = database.getChat(chatID).orElseThrow(NotFoundException::new);

        if (!(chat instanceof TauChannel channel)) {
            throw new WrongAddressException();
        }

        if (!chat.getMembers().contains(session.member)) {
            throw new NotFoundException();
        }

        Member member = database
                .findMemberByNickname(otherNickname)
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
        InviteCodeObject token = new InviteCodeObject(database, channel, member, session.member, expiresDate);

        token.save();

        sendFin(new TextMessage(new Headers().setType(TauMessageTypes.CHANNEL), token.getCode()));
    }

    private void LEAVE(@NotNull ChannelRequest request) throws Exception {
        TauDatabase database = (TauDatabase) session.server.serverConfig.database();
        TauGroupManager manager = (TauGroupManager) session.server.serverConfig.groupManager();

        UUID chatID = request.chatID;

        TauChat tauChat = database.getChat(chatID).orElseThrow(NotFoundException::new);

        if (!(tauChat instanceof TauChannel channel)) {
            throw new WrongAddressException();
        }

        if (channel.owner().equals(session.member)) {
            throw new UnauthorizedException();
        }

        database.leaveFromChat(channel, session.member);

        ChatMessage chatMessage = SysMessages.channelLeave.chatMessage(channel, session.member);

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

    private void CODES(ChannelRequest request) throws Exception {
        if (session.member == null) {
            throw new UnauthorizedException();
        }

        TauDatabase database = (TauDatabase) session.member.database();

        UUID chatID = request.chatID;

        TauChat chat = database.getChat(chatID).orElseThrow(NotFoundException::new);

        if (!(chat instanceof TauChannel channel)) {
            throw new WrongAddressException();
        }

        Collection<InviteCodeObject> codes = channel.getActiveInviteCodes();

        Collection<TauCode> collected = codes.stream()
                .map(c -> new InviteTauCode(c, channel.title(), c.getNickname(), c.getSenderNickname()))
                .collect(Collectors.toSet());
        sendFin(new CodeListMessage(new Headers().setType(TauMessageTypes.CHANNEL), collected));
    }

    private void MY_INVITES(@NotNull ChannelRequest request) throws Exception {
        if (session.member == null) {
            throw new UnauthorizedException();
        }

        TauDatabase database = (TauDatabase) session.member.database();

        Collection<InviteCodeObject> myInvites = database.getInviteCodesByNickname(session.member);

        Collection<TauCode> collected = new HashSet<>();
        for (InviteCodeObject c : myInvites) {
            UUID chatID = c.getChatID();
            TauChat chat = database.getChat(chatID).orElseThrow(NotFoundException::new);

            if (!(chat instanceof TauChannel channel)) {
                throw new WrongAddressException();
            }

            InviteTauCode inviteTauCode = new InviteTauCode(
                    c,
                    channel.title(),
                    c.getNickname(),
                    c.getSenderNickname()
            );
            collected.add(inviteTauCode);
        }

        sendFin(new CodeListMessage(
                new Headers().setType(TauMessageTypes.CHANNEL),
                collected
        ));
    }
}
