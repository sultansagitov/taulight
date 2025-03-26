package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.receiver.ServerChain;
import net.result.sandnode.db.Member;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.exception.error.ServerSandnodeErrorException;
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

import java.util.UUID;

public class UseCodeServerChain extends ServerChain  implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(UseCodeServerChain.class);

    public UseCodeServerChain(Session session) {
        super(session);
    }

    // TODO check for session.member is member from code
    @Override
    public void sync() throws Exception {
        var request = new UseCodeRequest(queue.take());
        String code = request.content();

        if (session.member == null) {
            throw new UnauthorizedException();
        }

        TauDatabase database = (TauDatabase) session.member.database();

        InviteCodeObject invite = database.getInviteCode(code).orElseThrow(NotFoundException::new);

        String nickname = invite.getNickname();
        UUID chatID = invite.getChatID();

        Member member = database
                .findMemberByNickname(nickname)
                .orElseThrow(() -> new ServerSandnodeErrorException("Member not found"));

        TauChat chat = database
                .getChat(chatID)
                .orElseThrow(() -> new ServerSandnodeErrorException("Channel not found"));

        if (!(chat instanceof TauChannel channel)) {
            throw new ServerSandnodeErrorException("Chat is not channel");
        }

        if (!invite.activate()) {
            throw new NoEffectException("Invite already activated");
        }

        channel.addMember(member);

        ChatMessage chatMessage = SysMessages.channelAdd.chatMessage(channel, member);

        try {
            TauHubProtocol.send(session, channel, chatMessage);
        } catch (NoEffectException e) {
            LOGGER.warn("Exception when sending system message of creating channel {}", e.getMessage());
        }

        sendFin(new HappyMessage());
    }
}
