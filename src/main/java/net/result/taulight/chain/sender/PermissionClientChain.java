package net.result.taulight.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.exception.ProtocolException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.db.Permission;
import net.result.taulight.message.types.PermissionRequest;

import java.util.UUID;

public class PermissionClientChain extends ClientChain {
    public PermissionClientChain(SandnodeClient client) {
        super(client);
    }

    public void addDefault(UUID chatID, Permission permission)
            throws ProtocolException, InterruptedException, SandnodeErrorException {
        sendAndReceive(PermissionRequest.def("+", chatID, permission)).expect(MessageTypes.HAPPY);
    }

    public void removeDefault(UUID chatID, Permission permission)
            throws ProtocolException, InterruptedException, SandnodeErrorException {
        sendAndReceive(PermissionRequest.def("-", chatID, permission)).expect(MessageTypes.HAPPY);
    }

    public void addRole(UUID chatID, UUID roleID, Permission permission)
            throws ProtocolException, InterruptedException, SandnodeErrorException {
        sendAndReceive(PermissionRequest.role("+", chatID, roleID, permission)).expect(MessageTypes.HAPPY);
    }

    public void removeRole(UUID chatID, UUID roleID, Permission permission)
            throws ProtocolException, InterruptedException, SandnodeErrorException {
        sendAndReceive(PermissionRequest.role("-", chatID, roleID, permission)).expect(MessageTypes.HAPPY);
    }
}
