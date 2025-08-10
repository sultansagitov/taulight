package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.ProtocolException;
import net.result.sandnode.exception.error.*;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.util.JPAUtil;
import net.result.taulight.cluster.ChatCluster;
import net.result.taulight.cluster.TauClusterManager;
import net.result.taulight.db.*;
import net.result.taulight.dto.*;
import net.result.taulight.message.CodeListMessage;
import net.result.taulight.message.TauMessageTypes;
import net.result.taulight.message.types.CodeRequest;
import net.result.taulight.message.types.CodeResponse;
import net.result.taulight.util.ChatUtil;
import net.result.taulight.util.SysMessages;
import net.result.taulight.util.TauHubProtocol;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Collectors;

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

        CodeRequestDTO.GroupCodes groupCodes = request.groupCodes();
        if (groupCodes != null) {
            return handleGroupCodes(groupCodes, tauMember);
        }

        if (request.myCodes()) {
            return handleMyCodes(tauMember);
        }

        throw new TooFewArgumentsException();
    }

    private Message handleCheck(CodeRequestDTO.Check check, TauMemberEntity you)
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

    private Message handleUse(CodeRequestDTO.Use use, MemberEntity you)
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

    private Message handleGroupCodes(CodeRequestDTO.GroupCodes dto, TauMemberEntity you) throws Exception {
        ChatUtil chatUtil = session.server.container.get(ChatUtil.class);

        ChatEntity chat = chatUtil.getChat(dto.chatID).orElseThrow(NotFoundException::new);
        if (!chatUtil.contains(chat, you)) throw new UnauthorizedException();
        if (!(chat instanceof GroupEntity group)) throw new WrongAddressException();

        Headers headers = new Headers().setType(TauMessageTypes.CODE);

        Collection<CodeDTO> collected = group.inviteCodes().stream()
                .map(InviteCodeDTO::new)
                .collect(Collectors.toSet());

        return new CodeListMessage(headers, collected);
    }

    private Message handleMyCodes(@NotNull TauMemberEntity you) {
        Collection<CodeDTO> collected = you
                .inviteCodesAsReceiver().stream()
                .map(InviteCodeDTO::new)
                .collect(Collectors.toSet());

        return new CodeListMessage(new Headers().setType(TauMessageTypes.CODE), collected);
    }

}
