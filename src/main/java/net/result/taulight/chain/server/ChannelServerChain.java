package net.result.taulight.chain.server;

import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.exception.DatabaseException;
import net.result.taulight.SysMessages;
import net.result.taulight.TauAgentProtocol;
import net.result.taulight.TauHubProtocol;
import net.result.taulight.db.ChatMessage;
import net.result.taulight.db.TauDatabase;
import net.result.sandnode.db.Member;
import net.result.sandnode.error.Errors;
import net.result.sandnode.exception.DeserializationException;
import net.result.taulight.exception.MessageNotForwardedException;
import net.result.taulight.group.TauGroupManager;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.serverclient.ClientMember;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.error.TauErrors;
import net.result.taulight.group.TauChatGroup;
import net.result.taulight.message.types.ChannelRequest;
import net.result.taulight.db.TauChannel;
import net.result.taulight.db.TauChat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.UUID;

public class ChannelServerChain extends ServerChain {
    private static final Logger LOGGER = LogManager.getLogger(ChannelServerChain.class);

    public ChannelServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, DeserializationException {
        ChannelRequest request = new ChannelRequest(queue.take());
        ChannelRequest.DataType type = request.object.type;

        TauDatabase database = (TauDatabase) session.server.serverConfig.database();
        TauGroupManager manager = (TauGroupManager) session.server.serverConfig.groupManager();

        if (type == null) {
            sendFin(Errors.TOO_FEW_ARGS.createMessage());
            return;
        }

        switch (type) {
            case NEW -> {
                String title = request.object.title;

                if (title == null) {
                    sendFin(Errors.TOO_FEW_ARGS.createMessage());
                    return;
                }

                TauChannel channel = new TauChannel(title, session.member);
                try {
                    database.saveChat(channel);
                    database.addMemberToChat(channel, session.member);
                } catch (DatabaseException e) {
                    LOGGER.error(e);
                    sendFin(Errors.SERVER_ERROR.createMessage());
                    return;
                }

                TauAgentProtocol.addMemberToGroup(session, manager.getGroup(channel));

                ChatMessage chatMessage = SysMessages.channelNew.chatMessage(channel, session.member);

                try {
                    TauHubProtocol.send(session.server.serverConfig, channel, chatMessage);
                } catch (DatabaseException | MessageNotForwardedException e) {
                    LOGGER.warn("Exception when sending system message of creating channel {}", e.getMessage());
                }

                send(new HappyMessage());
            }
            case ADD -> {
                UUID chatID = request.object.chatID;
                ClientMember cMember = request.object.member;

                if (chatID == null || cMember == null) {
                    sendFin(Errors.TOO_FEW_ARGS.createMessage());
                    return;
                }

                Optional<TauChat> optChat;
                Optional<Member> optMember;
                try {
                    optChat = database.getChat(chatID);
                    optMember = database.findMemberByMemberID(cMember.memberID);
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

                if (!channel.getOwner().equals(session.member)) {
                    sendFin(Errors.UNAUTHORIZED.createMessage());
                    return;
                }

                try {
                    database.addMemberToChat(channel, member);
                } catch (DatabaseException e) {
                    LOGGER.error(e);
                    sendFin(Errors.SERVER_ERROR.createMessage());
                    return;
                }

                TauChatGroup tauChatGroup = manager.getGroup(channel);
                TauAgentProtocol.addMemberToGroup(session, member, tauChatGroup);

                ChatMessage chatMessage = SysMessages.channelAdd.chatMessage(channel, session.member);

                try {
                    TauHubProtocol.send(session.server.serverConfig, channel, chatMessage);
                } catch (DatabaseException | MessageNotForwardedException e) {
                    LOGGER.warn("Exception when sending system message of adding member: {}", e.getMessage());
                }

                send(new HappyMessage());
            }
            case LEAVE -> {
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

                if (channel.getOwner().equals(session.member)) {
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
                    TauHubProtocol.send(session.server.serverConfig, channel, chatMessage);
                } catch (DatabaseException | MessageNotForwardedException e) {
                    LOGGER.warn("Exception when sending system message of leaving member: {}", e.getMessage());
                }

                send(new HappyMessage());
            }
        }
    }
}
