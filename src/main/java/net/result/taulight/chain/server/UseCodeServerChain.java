package net.result.taulight.chain.server;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.server.ServerChain;
import net.result.sandnode.db.Member;
import net.result.sandnode.error.Errors;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.SysMessages;
import net.result.taulight.TauHubProtocol;
import net.result.taulight.db.*;
import net.result.sandnode.exception.error.NoEffectException;
import net.result.taulight.message.types.UseCodeRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.UUID;

public class UseCodeServerChain extends ServerChain  implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(UseCodeServerChain.class);

    public UseCodeServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, ExpectedMessageException, UnprocessedMessagesException {
        var request = new UseCodeRequest(queue.take());
        String code = request.content();

        if (session.member == null) {
            sendFin(Errors.UNAUTHORIZED.createMessage());
            return;
        }

        TauDatabase database = (TauDatabase) session.member.database();

        Optional<InviteToken> inviteToken;
        try {
            inviteToken = database.getInviteToken(code);
        } catch (DatabaseException e) {
            LOGGER.error(e);
            sendFin(Errors.SERVER_ERROR.createMessage());
            return;
        }

        if (inviteToken.isEmpty()) {
            sendFin(Errors.NOT_FOUND.createMessage());
            return;
        }

        String nickname = inviteToken.get().getNickname();

        Optional<Member> member;
        try {
            member = database.findMemberByNickname(nickname);
        } catch (DatabaseException e) {
            LOGGER.error(e);
            sendFin(Errors.SERVER_ERROR.createMessage());
            return;
        }

        if (member.isEmpty()) {
            LOGGER.error("Member by nickname {}", nickname);
            sendFin(Errors.SERVER_ERROR.createMessage());
            return;
        }

        UUID chatID = inviteToken.get().getChatID();
        Optional<TauChat> chat;
        try {
            chat = database.getChat(chatID);
        } catch (DatabaseException e) {
            LOGGER.error(e);
            sendFin(Errors.SERVER_ERROR.createMessage());
            return;
        }

        if (chat.isEmpty()) {
            LOGGER.error("Chat not found");
            sendFin(Errors.SERVER_ERROR.createMessage());
            return;
        }

        if (!(chat.get() instanceof TauChannel channel)) {
            LOGGER.error("Chat is not channel");
            sendFin(Errors.SERVER_ERROR.createMessage());
            return;
        }

        try {
            if (inviteToken.get().activate()) {
                channel.addMember(member.get());

                ChatMessage chatMessage = SysMessages.channelAdd.chatMessage(channel, member.get());

                try {
                    TauHubProtocol.send(session, channel, chatMessage);
                } catch (NoEffectException e) {
                    LOGGER.warn("Exception when sending system message of creating channel {}", e.getMessage());
                } catch (UnauthorizedException e) {
                    sendFin(Errors.UNAUTHORIZED.createMessage());
                    return;
                }
            } else {
                sendFin(Errors.NO_EFFECT.createMessage());
                return;
            }
        } catch (DatabaseException e) {
            LOGGER.error(e);
            sendFin(Errors.SERVER_ERROR.createMessage());
            return;
        }

        sendFin(new HappyMessage());
    }
}
