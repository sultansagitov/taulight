package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.util.SysMessages;
import net.result.taulight.util.TauHubProtocol;
import net.result.taulight.db.ChannelEntity;
import net.result.taulight.db.InviteCodeEntity;
import net.result.taulight.db.TauDatabase;
import net.result.taulight.db.TauMemberEntity;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.sandnode.exception.error.NoEffectException;
import net.result.taulight.message.types.UseCodeRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UseCodeServerChain extends ServerChain  implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(UseCodeServerChain.class);

    public UseCodeServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws Exception {
        var request = new UseCodeRequest(queue.take());
        String code = request.content();

        if (session.member == null) {
            throw new UnauthorizedException();
        }

        TauDatabase database = (TauDatabase) session.server.serverConfig.database();

        InviteCodeEntity invite = database.findInviteCode(code).orElseThrow(NotFoundException::new);

        TauMemberEntity member = invite.receiver();
        ChannelEntity channel = invite.channel();

        if (invite.receiver() != session.member.tauMember()) {
            //TODO add channel roles and use it
            if (invite.sender() == session.member.tauMember()) {
                throw new UnauthorizedException();
            } else {
                throw new NotFoundException();
            }
        }

        if (!database.activateInviteCode(invite)) {
            throw new NoEffectException("Invite already activated");
        }

        if (!database.addMemberToChannel(channel, member)) {
            throw new NoEffectException();
        }

        ChatMessageInputDTO input = SysMessages.channelAdd.toInput(channel, member);

        try {
            TauHubProtocol.send(session, channel, input);
        } catch (NoEffectException e) {
            LOGGER.warn("Exception when sending system message of creating channel {}", e.getMessage());
        }

        sendFin(new HappyMessage());
    }
}
