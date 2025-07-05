package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.db.MemberRepository;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.*;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.db.*;
import net.result.taulight.dto.RoleDTO;
import net.result.taulight.dto.RoleRequestDTO;
import net.result.taulight.dto.RolesDTO;
import net.result.taulight.message.types.RoleRequest;
import net.result.taulight.message.types.RoleResponse;
import net.result.taulight.util.ChatUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class RoleServerChain extends ServerChain implements ReceiverChain {

    public RoleServerChain(Session session) {
        super(session);
    }

    @Override
    public RoleResponse handle(RawMessage raw) throws Exception {
        if (session.member == null) throw new UnauthorizedException();

        ChatUtil chatUtil = session.server.container.get(ChatUtil.class);

        RoleRequest request = new RoleRequest(raw);

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

        Set<UUID> memberRoles = roles.stream()
                .filter(role -> role.members().contains(session.member.tauMember()))
                .map(RoleEntity::id)
                .collect(Collectors.toSet());

        Set<Permission> permissions = group.permissions();

        return switch (dataType) {
            case GET -> get(allRoles, memberRoles, permissions);
            case CREATE -> create(group, roleName, allRoles, memberRoles, permissions);
            case ADD -> add(roleName, nickname, roles, allRoles, memberRoles, permissions);
        };
    }

    private @NotNull RoleResponse get(Set<RoleDTO> allRoles, Set<UUID> memberRoles, Set<Permission> permissions) {
        RolesDTO dto = new RolesDTO(allRoles, memberRoles, permissions);
        return new RoleResponse(dto);
    }

    private @NotNull RoleResponse create(GroupEntity group, String roleName, Set<RoleDTO> allRoles, Set<UUID> memberRoles, Set<Permission> permissions) throws TooFewArgumentsException, DatabaseException {
        RoleRepository roleRepo = session.server.container.get(RoleRepository.class);

        if (roleName == null || roleName.trim().isEmpty()) throw new TooFewArgumentsException();
        RoleEntity newRole = roleRepo.create(group, roleName);
        allRoles.add(new RoleDTO(newRole));
        return new RoleResponse(new RolesDTO(allRoles, memberRoles, permissions));
    }

    private @NotNull RoleResponse add(String roleName, String nickname, Set<RoleEntity> roles, Set<RoleDTO> allRoles, Set<UUID> memberRoles, Set<Permission> permissions) throws TooFewArgumentsException, NotFoundException, DatabaseException, NoEffectException {
        RoleRepository roleRepo = session.server.container.get(RoleRepository.class);
        MemberRepository memberRepo = session.server.container.get(MemberRepository.class);

        if (roleName == null || nickname == null) throw new TooFewArgumentsException();

        RoleEntity roleToAdd = roles.stream()
                .filter(role -> role.name().equals(roleName))
                .findFirst().orElseThrow(NotFoundException::new);

        TauMemberEntity member = memberRepo
                .findByNickname(nickname)
                .orElseThrow(NotFoundException::new)
                .tauMember();

        if (!roleRepo.addMember(roleToAdd, member)) throw new NoEffectException();
        return new RoleResponse(new RolesDTO(allRoles, memberRoles, permissions));
    }
}
