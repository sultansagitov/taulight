package net.result.taulight.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.db.Permission;
import net.result.taulight.message.types.PermissionRequest;

import java.util.UUID;

public class PermissionClientChain extends ClientChain {
    public PermissionClientChain(SandnodeClient client) {
        super(client);
    }

    public void addDefault(UUID chatID, Permission permission) throws UnprocessedMessagesException,
            InterruptedException, UnknownSandnodeErrorException, SandnodeErrorException, ExpectedMessageException {
        reqRes(PermissionRequest.def("+", chatID, permission));
    }

    public void removeDefault(UUID chatID, Permission permission) throws UnprocessedMessagesException,
            InterruptedException, UnknownSandnodeErrorException, SandnodeErrorException, ExpectedMessageException {
        reqRes(PermissionRequest.def("-", chatID, permission));
    }

    public void addRole(UUID chatID, UUID roleID, Permission permission) throws UnprocessedMessagesException,
            InterruptedException, UnknownSandnodeErrorException, SandnodeErrorException, ExpectedMessageException {
        reqRes(PermissionRequest.role("+", chatID, roleID, permission));
    }

    public void removeRole(UUID chatID, UUID roleID, Permission permission) throws UnprocessedMessagesException,
            InterruptedException, UnknownSandnodeErrorException, SandnodeErrorException, ExpectedMessageException {
        reqRes(PermissionRequest.role("-", chatID, roleID, permission));
    }

    private void reqRes(PermissionRequest request) throws UnprocessedMessagesException, InterruptedException,
            UnknownSandnodeErrorException, SandnodeErrorException, ExpectedMessageException {
        send(request);
        new HappyMessage(receive());
    }
}
