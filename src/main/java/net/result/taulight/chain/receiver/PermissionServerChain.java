package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.SandnodeException;
import net.result.sandnode.exception.error.NoEffectException;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.util.Container;
import net.result.taulight.db.*;
import net.result.taulight.message.types.PermissionRequest;

public class PermissionServerChain extends ServerChain implements ReceiverChain {

    private RoleRepository roleRepo;
    private GroupRepository groupRepo;

    public PermissionServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws Exception {
        PermissionRequest request = new PermissionRequest(queue.take());

        Container container = session.server.container;
        roleRepo = container.get(RoleRepository.class);
        groupRepo = container.get(GroupRepository.class);

        boolean success;
        if (request.mode.equals("-")) {
            success = revokePermission(request);
        } else {
            success = grantPermission(request);
        }

        if (!success) {
            throw new NoEffectException();
        }

        send(new HappyMessage());
    }

    private GroupEntity getGroup(PermissionRequest request) throws SandnodeException {
        GroupEntity group = groupRepo.findById(request.chatID).orElseThrow(NotFoundException::new);
        if (session.member == null && !group.owner().equals(session.member.tauMember())) {
            throw new UnauthorizedException();
        }
        return group;
    }

    private RoleEntity getRole(PermissionRequest request) throws SandnodeException {
        RoleEntity role = roleRepo.findById(request.roleID).orElseThrow(NotFoundException::new);
        GroupEntity group = role.group();
        if (session.member == null && !group.owner().equals(session.member.tauMember())) {
            throw new UnauthorizedException();
        }
        return role;
    }

    private boolean grantPermission(PermissionRequest request) throws SandnodeException {
        if (request.roleID != null) {
            RoleEntity role = getRole(request);
            return grantRolePermission(role, request.perm);
        } else {
            GroupEntity group = getGroup(request);
            return grantChatPermission(group, request.perm);
        }
    }

    private boolean revokePermission(PermissionRequest request) throws SandnodeException {
        if (request.roleID != null) {
            RoleEntity role = getRole(request);
            return revokeRolePermission(role, request.perm);
        } else {
            GroupEntity group = getGroup(request);
            return revokeChatPermission(group, request.perm);
        }
    }

    private boolean grantRolePermission(RoleEntity role, Permission perm) throws DatabaseException {
        return roleRepo.grantPermission(role, perm);
    }

    private boolean revokeRolePermission(RoleEntity role, Permission perm) throws DatabaseException {
        return roleRepo.revokePermission(role, perm);
    }

    private boolean grantChatPermission(GroupEntity group, Permission perm) throws DatabaseException {
        return groupRepo.grantPermission(group, perm);
    }

    private boolean revokeChatPermission(GroupEntity group, Permission perm) throws DatabaseException {
        return groupRepo.revokePermission(group, perm);
    }
}
