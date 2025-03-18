package net.result.taulight.chain.server;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.TooFewArgumentsException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.TextMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import net.result.taulight.db.*;
import net.result.taulight.exception.AlreadyExistingRecordException;
import net.result.taulight.SysMessages;
import net.result.taulight.TauAgentProtocol;
import net.result.taulight.TauHubProtocol;
import net.result.sandnode.db.Member;
import net.result.sandnode.error.Errors;
import net.result.sandnode.exception.error.NoEffectException;
import net.result.taulight.group.TauGroupManager;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.error.TauErrors;
import net.result.taulight.message.TauMessageTypes;
import net.result.taulight.message.types.ChannelRequest;
import net.result.taulight.message.types.UUIDMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

public class ChannelServerChain extends ServerChain implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(ChannelServerChain.class);

    public ChannelServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, DeserializationException, UnprocessedMessagesException,
            TooFewArgumentsException, ExpectedMessageException {
        ChannelRequest request = new ChannelRequest(queue.take());

        if (request.type == null) {
            sendFin(Errors.TOO_FEW_ARGS.createMessage());
            return;
        }

        switch (request.type) {
            case NEW -> NEW(request);
            case ADD -> ADD(request);
            case LEAVE -> LEAVE(request);
        }
    }

    private void NEW(@NotNull ChannelRequest request) throws UnprocessedMessagesException, InterruptedException {
        TauDatabase database = (TauDatabase) session.server.serverConfig.database();
        TauGroupManager manager = (TauGroupManager) session.server.serverConfig.groupManager();

        String title = request.title;

        if (title == null) {
            sendFin(Errors.TOO_FEW_ARGS.createMessage());
            return;
        }

        TauChannel channel = new TauChannel(database, title, session.member);
        try {
            while (true) {
                channel.setRandomID();
                try {
                    channel.save();
                    break;
                } catch (AlreadyExistingRecordException ignored) {
                }
            }

            channel.addMember(session.member);
        } catch (DatabaseException e) {
            LOGGER.error(e);
            sendFin(Errors.SERVER_ERROR.createMessage());
            return;
        }

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

    private void ADD(@NotNull ChannelRequest request) throws UnprocessedMessagesException, InterruptedException {
        if (session.member == null) {
            sendFin(Errors.UNAUTHORIZED.createMessage());
            return;
        }

        TauDatabase database = (TauDatabase) session.member.database();

        UUID chatID = request.chatID;
        String otherNickname = request.otherNickname;

        Optional<TauChat> optChat;
        Optional<Member> optMember;
        try {
            optChat = database.getChat(chatID);
            optMember = database.findMemberByNickname(otherNickname);
        } catch (DatabaseException e) {
            LOGGER.error(e);
            sendFin(Errors.SERVER_ERROR.createMessage());
            return;
        }

        if (optChat.isEmpty()) {
            sendFin(TauErrors.CHAT_NOT_FOUND.createMessage());
            return;
        }

        if (optMember.isEmpty()) {
            sendFin(Errors.ADDRESSED_MEMBER_NOT_FOUND.createMessage());
            return;
        }

        Member member = optMember.get();

        if (!(optChat.get() instanceof TauChannel channel)) {
            sendFin(Errors.WRONG_ADDRESS.createMessage());
            return;
        }

        //TODO add settings for inviting by another members
        if (!channel.owner().equals(session.member)) {
            sendFin(Errors.UNAUTHORIZED.createMessage());
            return;
        }

        ZonedDateTime expiresDate = ZonedDateTime.now().plusDays(1);
        InviteCodeObject token = new InviteCodeObject(database, channel, member, session.member, expiresDate);

        try {
            token.save();
        } catch (DatabaseException e) {
            LOGGER.error("Error while saving invite token", e);
            sendFin(Errors.SERVER_ERROR.createMessage());
            return;
        }

        sendFin(new TextMessage(new Headers().setType(TauMessageTypes.CHANNEL), token.getCode()));
    }

    private void LEAVE(@NotNull ChannelRequest request) throws UnprocessedMessagesException, InterruptedException {
        TauDatabase database = (TauDatabase) session.server.serverConfig.database();
        TauGroupManager manager = (TauGroupManager) session.server.serverConfig.groupManager();

        UUID chatID = request.chatID;

        Optional<TauChat> optChat;
        try {
            optChat = database.getChat(chatID);
        } catch (DatabaseException e) {
            sendFin(Errors.SERVER_ERROR.createMessage());
            return;
        }

        if (optChat.isEmpty()) {
            sendFin(TauErrors.CHAT_NOT_FOUND.createMessage());
            return;
        }

        TauChat tauChat = optChat.get();

        if (!(tauChat instanceof TauChannel channel)) {
            sendFin(Errors.WRONG_ADDRESS.createMessage());
            return;
        }

        if (channel.owner().equals(session.member)) {
            sendFin(Errors.UNAUTHORIZED.createMessage());
            return;
        }

        try {
            database.leaveFromChat(channel, session.member);
        } catch (DatabaseException e) {
            send(Errors.SERVER_ERROR.createMessage());
        }

        TauAgentProtocol.removeMemberFromGroup(session, manager.getGroup(channel));

        ChatMessage chatMessage = SysMessages.channelLeave.chatMessage(channel, session.member);

        try {
            TauHubProtocol.send(session, channel, chatMessage);
        } catch (UnauthorizedException e) {
            throw new ImpossibleRuntimeException(e);
        } catch (DatabaseException | NoEffectException e) {
            LOGGER.warn("Exception when sending system message of leaving member: {}", e.getMessage());
        }

        send(new HappyMessage());
    }
}
