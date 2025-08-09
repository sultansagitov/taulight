package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.ProtocolException;
import net.result.sandnode.exception.error.NoEffectException;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.util.JPAUtil;
import net.result.taulight.cluster.ChatCluster;
import net.result.taulight.cluster.TauClusterManager;
import net.result.taulight.db.*;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.dto.CodeRequestDTO;
import net.result.taulight.dto.CodeResponseDTO;
import net.result.taulight.dto.InviteCodeDTO;
import net.result.taulight.message.types.CodeRequest;
import net.result.taulight.message.types.CodeResponse;
import net.result.taulight.util.SysMessages;
import net.result.taulight.util.TauHubProtocol;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class CodeServerChain extends ServerChain implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(CodeServerChain.class);

    public CodeServerChain(Session session) {
        super(session);
    }

    @Override
    public Message handle(RawMessage raw) throws Exception {
        CodeRequest request = new CodeRequest(raw);

        if (session.member == null) {
            throw new UnauthorizedException();
        }

        TauMemberEntity tauMember = session.member.tauMember();

        CodeRequestDTO.Check check = request.check();
        if (check != null) {
            return handleCheck(check, tauMember);
        }

        CodeRequestDTO.Use use = request.use();
        if (use != null) {
            return handleUse(use, session.member);
        }

        return null;
    }

    private @NotNull Message handleCheck(CodeRequestDTO.Check check, TauMemberEntity you)
            throws NotFoundException, DatabaseException {
        InviteCodeRepository inviteCodeRepo = session.server.container.get(InviteCodeRepository.class);

        InviteCodeEntity invite = inviteCodeRepo.find(check.code).orElseThrow(NotFoundException::new);

        if (!invite.receiver().equals(you)) {
            //TODO add group roles and use it
            if (!invite.sender().equals(you)) {
                throw new NotFoundException();
            }
        }

        var code = new InviteCodeDTO(invite);
        return new CodeResponse(new CodeResponseDTO(new CodeResponseDTO.Check(code)));
    }

    private @NotNull Message handleUse(CodeRequestDTO.Use use, MemberEntity you)
            throws DatabaseException, InterruptedException, SandnodeErrorException, ProtocolException {
        JPAUtil jpaUtil = session.server.container.get(JPAUtil.class);
        TauClusterManager tauClusterManager = session.server.container.get(TauClusterManager.class);
        GroupRepository groupRepo = session.server.container.get(GroupRepository.class);
        InviteCodeRepository inviteCodeRepo = session.server.container.get(InviteCodeRepository.class);

        InviteCodeEntity invite = inviteCodeRepo.find(use.code).orElseThrow(NotFoundException::new);

        TauMemberEntity member = invite.receiver();
        GroupEntity group = invite.group();

        if (!invite.receiver().equals(you.tauMember())) {
            //TODO add group roles and use it
            if (invite.sender().equals(you.tauMember())) {
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

        session.member = jpaUtil.refresh(you);

        ChatCluster cluster = tauClusterManager.getCluster(group);

        for (Session agent : session.server.getAgents()) {
            //noinspection DataFlowIssue
            if (you.equals(agent.member)) {
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
