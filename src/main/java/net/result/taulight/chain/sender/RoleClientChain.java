package net.result.taulight.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.dto.RolesDTO;
import net.result.taulight.message.types.RoleRequest;
import net.result.taulight.message.types.RoleResponse;

import java.util.UUID;

public class RoleClientChain extends ClientChain {
    public RoleClientChain(SandnodeClient client) {
        super(client);
    }

    public synchronized RolesDTO getRoles(UUID chatID) throws UnprocessedMessagesException, InterruptedException,
            UnknownSandnodeErrorException, SandnodeErrorException, DeserializationException, ExpectedMessageException {
        send(RoleRequest.getRoles(chatID));

        RawMessage raw = receive();
        ServerErrorManager.instance().handleError(raw);

        return new RoleResponse(raw).roles();
    }

    public synchronized void addRole(UUID chatID, String roleName) throws UnprocessedMessagesException,
            InterruptedException, UnknownSandnodeErrorException, SandnodeErrorException {
        send(RoleRequest.addRole(chatID, roleName));

        RawMessage raw = receive();
        ServerErrorManager.instance().handleError(raw);
    }

    public synchronized void assignRole(UUID chatID, String nickname, String roleName)
            throws UnprocessedMessagesException, InterruptedException, UnknownSandnodeErrorException,
            SandnodeErrorException {
        send(RoleRequest.assignRole(chatID, roleName, nickname));

        RawMessage raw = receive();
        ServerErrorManager.instance().handleError(raw);
    }
}
