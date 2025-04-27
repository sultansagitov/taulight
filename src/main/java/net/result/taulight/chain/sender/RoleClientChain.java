package net.result.taulight.chain.sender;

import net.result.sandnode.chain.sender.ClientChain;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.util.IOController;
import net.result.taulight.dto.RolesDTO;
import net.result.taulight.message.types.RoleRequest;
import net.result.taulight.message.types.RoleResponse;

import java.util.UUID;

public class RoleClientChain extends ClientChain {
    public RoleClientChain(IOController io) {
        super(io);
    }

    public synchronized RolesDTO getRoles(UUID chatID) throws UnprocessedMessagesException, InterruptedException,
            UnknownSandnodeErrorException, SandnodeErrorException, DeserializationException, ExpectedMessageException {
        send(RoleRequest.getRoles(chatID));

        RawMessage raw = queue.take();
        ServerErrorManager.instance().handleError(raw);

        return new RoleResponse(raw).roles();
    }

    public synchronized void addRole(UUID chatID, String roleName) throws UnprocessedMessagesException,
            InterruptedException, UnknownSandnodeErrorException, SandnodeErrorException, ExpectedMessageException,
            DeserializationException {
        send(RoleRequest.addRole(chatID, roleName));

        RawMessage raw = queue.take();
        ServerErrorManager.instance().handleError(raw);
    }

    public synchronized void assignRole(UUID chatID, String nickname, String roleName)
            throws UnprocessedMessagesException, InterruptedException, UnknownSandnodeErrorException,
            SandnodeErrorException, ExpectedMessageException, DeserializationException {
        send(RoleRequest.assignRole(chatID, roleName, nickname));

        RawMessage raw = queue.take();
        ServerErrorManager.instance().handleError(raw);
    }
}
