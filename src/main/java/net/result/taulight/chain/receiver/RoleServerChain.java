package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.exception.error.*;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.db.*;
import net.result.taulight.dto.RolesDTO;
import net.result.taulight.message.types.RoleRequest;
import net.result.taulight.message.types.RoleResponse;

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

        TauDatabase database = (TauDatabase) session.server.serverConfig.database();

        RoleRequest request = new RoleRequest(queue.take());

        RoleRequest.DataType dataType = request.getDataType();
        UUID chatID = request.getChatID();
        String roleName = request.getRoleName();
        String nickname = request.getNickname();

        ChatEntity chat = database.getChat(chatID).orElseThrow(NotFoundException::new);
        if (!database.getMembers(chat).contains(session.member.tauMember())) throw new NotFoundException();
        if (!(chat instanceof ChannelEntity channel)) throw new WrongAddressException();

        if (!channel.owner().equals(session.member.tauMember())) throw new UnauthorizedException();

        Set<RoleEntity> roles = channel.roles();
        Set<String> allRoles = roles.stream()
                .map(RoleEntity::name)
                .collect(Collectors.toSet());

        Set<String> memberRoles = roles.stream()
                .filter(role -> role.members().contains(session.member.tauMember()))
                .map(RoleEntity::name)
                .collect(Collectors.toSet());

        switch (dataType) {
            case GET:
                RolesDTO dto = new RolesDTO(allRoles, memberRoles);
                sendFin(new RoleResponse(dto));
                return;

            case CREATE:
                if (roleName == null || roleName.trim().isEmpty()) throw new TooFewArgumentsException();
                RoleEntity newRole = database.createRole(channel, roleName);
                allRoles.add(newRole.name());
                sendFin(new RoleResponse(new RolesDTO(allRoles, memberRoles)));
                return;

            case ADD:
                if (roleName == null || nickname == null) throw new TooFewArgumentsException();

                RoleEntity roleToAdd = roles.stream()
                        .filter(role -> role.name().equals(roleName))
                        .findFirst().orElseThrow(NotFoundException::new);

                TauMemberEntity member = database
                        .findMemberByNickname(nickname)
                        .orElseThrow(NotFoundException::new)
                        .tauMember();

                if (!database.addMemberToRole(roleToAdd, member)) throw new NoEffectException();
                sendFin(new RoleResponse(new RolesDTO(allRoles, memberRoles)));
        }
    }
}
