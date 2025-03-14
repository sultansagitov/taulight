package net.result.taulight.chain.server;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.ImpossibleRuntimeException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import net.result.taulight.exception.AlreadyExistingRecordException;
import net.result.taulight.SysMessages;
import net.result.taulight.TauAgentProtocol;
import net.result.taulight.TauHubProtocol;
import net.result.taulight.db.ChatMessage;
import net.result.taulight.db.TauDatabase;
import net.result.sandnode.db.Member;
import net.result.sandnode.error.Errors;
import net.result.sandnode.exception.DeserializationException;
import net.result.taulight.exception.error.MessageNotForwardedException;
import net.result.taulight.group.TauGroupManager;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.error.TauErrors;
import net.result.taulight.group.TauChatGroup;
import net.result.taulight.message.types.ChannelRequest;
import net.result.taulight.db.TauChannel;
import net.result.taulight.db.TauChat;
import net.result.taulight.message.types.UUIDMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class ChannelServerChain extends ServerChain implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(ChannelServerChain.class);

    public ChannelServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, DeserializationException, UnprocessedMessagesException {
        ChannelRequest request = new ChannelRequest(queue.take());
        ChannelRequest.DataType type = request.object.type;

        if (type == null) {
            sendFin(Errors.TOO_FEW_ARGS.createMessage());
            return;
        }

        switch (type) {
            case NEW -> NEW(request);
            case ADD -> ADD(request);
            case LEAVE -> LEAVE(request);
        }
    }

    private void NEW(@NotNull ChannelRequest request) throws UnprocessedMessagesException, InterruptedException {
        TauDatabase database = (TauDatabase) session.server.serverConfig.database();
        TauGroupManager manager = (TauGroupManager) session.server.serverConfig.groupManager();

        String title = request.object.title;

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
        } catch (DatabaseException | MessageNotForwardedException e) {
            LOGGER.warn("Exception when sending system message of creating channel {}", e.getMessage());
        }

        send(new UUIDMessage(new Headers().setType(MessageTypes.HAPPY), channel));
    }

    private void ADD(@NotNull ChannelRequest request) throws UnprocessedMessagesException, InterruptedException {
        TauDatabase database = (TauDatabase) session.server.serverConfig.database();
        TauGroupManager manager = (TauGroupManager) session.server.serverConfig.groupManager();

        UUID chatID = request.getChatID();
        String otherNickname = request.getOtherNickname();

        if (chatID == null || otherNickname == null) {
            sendFin(Errors.TOO_FEW_ARGS.createMessage());
            return;
        }

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

        if (!channel.owner().equals(session.member)) {
            sendFin(Errors.UNAUTHORIZED.createMessage());
            return;
        }

        try {
            channel.addMember(member);
        } catch (DatabaseException e) {
            LOGGER.error(e);
            sendFin(Errors.SERVER_ERROR.createMessage());
            return;
        }

        TauChatGroup tauChatGroup = manager.getGroup(channel);
        TauAgentProtocol.addMemberToGroup(session, member, tauChatGroup);

        ChatMessage chatMessage = SysMessages.channelAdd.chatMessage(channel, member);

        try {
            TauHubProtocol.send(session, channel, chatMessage);
        } catch (UnauthorizedException e) {
            throw new ImpossibleRuntimeException(e);
        } catch (DatabaseException | MessageNotForwardedException e) {
            LOGGER.warn("Exception when sending system message of adding member: {}", e.getMessage());
        }

        send(new HappyMessage());
    }

    private void LEAVE(@NotNull ChannelRequest request) throws UnprocessedMessagesException, InterruptedException {
        TauDatabase database = (TauDatabase) session.server.serverConfig.database();
        TauGroupManager manager = (TauGroupManager) session.server.serverConfig.groupManager();

        UUID chatID = request.object.chatID;

        if (chatID == null) {
            sendFin(Errors.TOO_FEW_ARGS.createMessage());
            return;
        }

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
        } catch (DatabaseException | MessageNotForwardedException e) {
            LOGGER.warn("Exception when sending system message of leaving member: {}", e.getMessage());
        }

        send(new HappyMessage());
    }
}
