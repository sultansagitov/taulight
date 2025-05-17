package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.db.*;
import net.result.taulight.util.SysMessages;
import net.result.taulight.util.TauHubProtocol;
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

        ChannelRepository channelRepo = session.server.container.get(ChannelRepository.class);
        InviteCodeRepository inviteCodeRepo = session.server.container.get(InviteCodeRepository.class);

        InviteCodeEntity invite = inviteCodeRepo.find(code).orElseThrow(NotFoundException::new);

        TauMemberEntity member = invite.receiver();
        ChannelEntity channel = invite.channel();

        if (!invite.receiver().equals(session.member.tauMember())) {
            //TODO add channel roles and use it
            if (invite.sender().equals(session.member.tauMember())) {
                throw new UnauthorizedException();
            } else {
                throw new NotFoundException();
            }
        }

        if (!inviteCodeRepo.activate(invite)) {
            throw new NoEffectException("Invite already activated");
        }

        if (!channelRepo.addMember(channel, member)) {
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
