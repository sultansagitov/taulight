package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.exception.error.NoEffectException;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.util.Container;
import net.result.taulight.db.Permission;
import net.result.taulight.entity.GroupEntity;
import net.result.taulight.entity.RoleEntity;
import net.result.taulight.message.types.PermissionRequest;
import net.result.taulight.repository.GroupRepository;
import net.result.taulight.repository.RoleRepository;

public class PermissionServerChain extends ServerChain implements ReceiverChain {
    private RoleRepository roleRepo;
    private GroupRepository groupRepo;
    private JPAUtil jpaUtil;

    @Override
    public HappyMessage handle(RawMessage raw) {
        PermissionRequest request = new PermissionRequest(raw);

        Container container = session.server.container;
        roleRepo = container.get(RoleRepository.class);
        groupRepo = container.get(GroupRepository.class);
        jpaUtil = container.get(JPAUtil.class);

        boolean success;
        if (request.mode.equals("-")) {
            success = revokePermission(request);
        } else {
            success = grantPermission(request);
        }

        if (!success) {
            throw new NoEffectException();
        }

        return new HappyMessage();
    }

    private GroupEntity getGroup(PermissionRequest request) {
        GroupEntity group = jpaUtil.find(GroupEntity.class, request.chatID).orElseThrow(NotFoundException::new);
        if (!group.getOwner().getMember().equals(session.member)) {
            throw new UnauthorizedException();
        }
        return group;
    }

    private RoleEntity getRole(PermissionRequest request) {
        RoleEntity role = jpaUtil
                .find(RoleEntity.class, request.roleID)
                .orElseThrow(NotFoundException::new);
        GroupEntity group = role.getGroup();
        if (!group.getOwner().getMember().equals(session.member)) {
            throw new UnauthorizedException();
        }
        return role;
    }

    private boolean grantPermission(PermissionRequest request) {
        if (request.roleID != null) {
            RoleEntity role = getRole(request);
            return grantRolePermission(role, request.perm);
        } else {
            GroupEntity group = getGroup(request);
            return grantChatPermission(group, request.perm);
        }
    }

    private boolean revokePermission(PermissionRequest request) {
        if (request.roleID != null) {
            RoleEntity role = getRole(request);
            return revokeRolePermission(role, request.perm);
        } else {
            GroupEntity group = getGroup(request);
            return revokeChatPermission(group, request.perm);
        }
    }

    private boolean grantRolePermission(RoleEntity role, Permission perm) {
        return roleRepo.grantPermission(role, perm);
    }

    private boolean revokeRolePermission(RoleEntity role, Permission perm) {
        return roleRepo.revokePermission(role, perm);
    }

    private boolean grantChatPermission(GroupEntity group, Permission perm) {
        return groupRepo.grantPermission(group, perm);
    }

    private boolean revokeChatPermission(GroupEntity group, Permission perm) {
        return groupRepo.revokePermission(group, perm);
    }
}
