package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.db.MemberRepository;
import net.result.sandnode.exception.error.*;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.db.*;
import net.result.taulight.dto.RoleDTO;
import net.result.taulight.dto.RoleRequestDTO;
import net.result.taulight.dto.RolesDTO;
import net.result.taulight.message.types.RoleRequest;
import net.result.taulight.message.types.RoleResponse;
import net.result.taulight.util.ChatUtil;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class RoleServerChain extends ServerChain implements ReceiverChain {

    public RoleServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws Exception {
        if (session.member == null) throw new UnauthorizedException();

        ChatUtil chatUtil = session.server.container.get(ChatUtil.class);
        MemberRepository memberRepo = session.server.container.get(MemberRepository.class);
        RoleRepository roleRepo = session.server.container.get(RoleRepository.class);

        RoleRequest request = new RoleRequest(queue.take());

        RoleRequestDTO.DataType dataType = request.dto().dataType;
        UUID chatID = request.dto().chatID;
        String roleName = request.dto().roleName;
        String nickname = request.dto().nickname;

        ChatEntity chat = chatUtil.getChat(chatID).orElseThrow(NotFoundException::new);
        if (!chatUtil.contains(chat, session.member.tauMember())) throw new NotFoundException();
        if (!(chat instanceof GroupEntity group)) throw new WrongAddressException();

        if (!group.owner().equals(session.member.tauMember())) throw new UnauthorizedException();

        Set<RoleEntity> roles = group.roles();
        Set<RoleDTO> allRoles = roles.stream()
                .map(RoleDTO::new)
                .collect(Collectors.toSet());

        Set<String> memberRoles = roles.stream()
                .filter(role -> role.members().contains(session.member.tauMember()))
                .map(RoleEntity::name)
                .collect(Collectors.toSet());

        Set<Permission> permissions = group.permissions();

        switch (dataType) {
            case GET:
                RolesDTO dto = new RolesDTO(allRoles, memberRoles, permissions);
                sendFin(new RoleResponse(dto));
                return;

            case CREATE:
                if (roleName == null || roleName.trim().isEmpty()) throw new TooFewArgumentsException();
                RoleEntity newRole = roleRepo.create(group, roleName);
                allRoles.add(new RoleDTO(newRole));
                sendFin(new RoleResponse(new RolesDTO(allRoles, memberRoles, permissions)));
                return;

            case ADD:
                if (roleName == null || nickname == null) throw new TooFewArgumentsException();

                RoleEntity roleToAdd = roles.stream()
                        .filter(role -> role.name().equals(roleName))
                        .findFirst().orElseThrow(NotFoundException::new);

                TauMemberEntity member = memberRepo
                        .findByNickname(nickname)
                        .orElseThrow(NotFoundException::new)
                        .tauMember();

                if (!roleRepo.addMember(roleToAdd, member)) throw new NoEffectException();
                sendFin(new RoleResponse(new RolesDTO(allRoles, memberRoles, permissions)));
        }
    }
}
