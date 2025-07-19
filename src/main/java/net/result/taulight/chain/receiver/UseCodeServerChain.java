package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.util.JPAUtil;
import net.result.taulight.db.*;
import net.result.taulight.cluster.ChatCluster;
import net.result.taulight.cluster.TauClusterManager;
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
    public HappyMessage handle(RawMessage raw) throws Exception {
        var request = new UseCodeRequest(raw);
        String code = request.content();

        if (session.member == null) {
            throw new UnauthorizedException();
        }

        JPAUtil jpaUtil = session.server.container.get(JPAUtil.class);
        GroupRepository groupRepo = session.server.container.get(GroupRepository.class);
        InviteCodeRepository inviteCodeRepo = session.server.container.get(InviteCodeRepository.class);
        TauClusterManager tauClusterManager = session.server.container.get(TauClusterManager.class);

        InviteCodeEntity invite = inviteCodeRepo.find(code).orElseThrow(NotFoundException::new);

        TauMemberEntity member = invite.receiver();
        GroupEntity group = invite.group();

        if (!invite.receiver().equals(session.member.tauMember())) {
            //TODO add group roles and use it
            if (invite.sender().equals(session.member.tauMember())) {
                throw new UnauthorizedException();
            } else {
                throw new NotFoundException();
            }
        }

        if (!inviteCodeRepo.activate(invite)) {
            throw new NoEffectException("Invite already activated");
        }

        if (!groupRepo.addMember(group, member)) {
            throw new NoEffectException();
        }

        session.member = jpaUtil.refresh(session.member);

        ChatCluster cluster = tauClusterManager.getCluster(group);

        for (Session agent : session.server.getAgents()) {
            //noinspection DataFlowIssue
            if (session.member.equals(agent.member)) {
                agent.addToCluster(cluster);
            }
        }

        ChatMessageInputDTO input = SysMessages.groupAdd.toInput(group, member);

        try {
            TauHubProtocol.send(session, group, input);
        } catch (NoEffectException e) {
            LOGGER.warn("Exception when sending system message of creating group {}", e.getMessage());
        }

        return new HappyMessage();
    }
}
