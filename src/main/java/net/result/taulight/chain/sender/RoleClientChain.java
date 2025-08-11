package net.result.taulight.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.dto.RolesDTO;
import net.result.taulight.message.types.RoleRequest;
import net.result.taulight.message.types.RoleResponse;

import java.util.UUID;

public class RoleClientChain extends ClientChain {
    public RoleClientChain(SandnodeClient client) {
        super(client);
    }

    public synchronized RolesDTO getRoles(UUID chatID)
            throws ProtocolException, InterruptedException, SandnodeErrorException {
        var raw = sendAndReceive(RoleRequest.getRoles(chatID));
        return new RoleResponse(raw).roles();
    }

    public synchronized void addRole(UUID chatID, String roleName)
            throws ProtocolException, InterruptedException, SandnodeErrorException {
        sendAndReceive(RoleRequest.addRole(chatID, roleName));
    }

    public synchronized void assignRole(UUID chatID, String nickname, UUID roleID)
            throws ProtocolException, InterruptedException, SandnodeErrorException {
        sendAndReceive(RoleRequest.assignRole(chatID, roleID, nickname));
    }
}
