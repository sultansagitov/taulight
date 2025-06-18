package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.SandnodeException;
import net.result.sandnode.exception.error.NoEffectException;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.util.Container;
import net.result.taulight.db.*;
import net.result.taulight.message.types.PermissionRequest;

import java.util.UUID;

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

        boolean b;
        if (request.mode.equals("-")) {
            b = revokePermission(request);
        } else {
            b = grantPermission(request);
        }

        if (!b) {
            throw new NoEffectException();
        }

        send(new HappyMessage());
    }

    private boolean grantPermission(PermissionRequest request) throws SandnodeException {
        if (request.roleID == null) {
            return grantChatPermission(request.chatID, request.perm);
        } else {
            return grantRolePermission(request.roleID, request.perm);
        }
    }

    private boolean revokePermission(PermissionRequest request) throws NotFoundException, DatabaseException {
        if (request.roleID != null) {
            return revokeRolePermission(request.roleID, request.perm);
        } else {
            return revokeChatPermission(request.chatID, request.perm);
        }
    }

    private boolean grantRolePermission(UUID roleID, Permission perm) throws DatabaseException, NotFoundException {
        RoleEntity role = roleRepo.findById(roleID).orElseThrow(NotFoundException::new);
        return roleRepo.grantPermission(role, perm);
    }

    private boolean revokeRolePermission(UUID roleID, Permission perm) throws DatabaseException, NotFoundException {
        RoleEntity role = roleRepo.findById(roleID).orElseThrow(NotFoundException::new);
        return roleRepo.revokePermission(role, perm);
    }

    private boolean grantChatPermission(UUID chatID, Permission perm) throws DatabaseException, NotFoundException {
        GroupEntity group = groupRepo.findById(chatID).orElseThrow(NotFoundException::new);
        return groupRepo.grantPermission(group, perm);
    }

    private boolean revokeChatPermission(UUID chatID, Permission perm) throws DatabaseException, NotFoundException {
        GroupEntity group = groupRepo.findById(chatID).orElseThrow(NotFoundException::new);
        return groupRepo.revokePermission(group, perm);
    }
}
