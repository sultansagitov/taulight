package net.result.taulight.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.util.IOController;
import net.result.sandnode.message.UUIDMessage;
import net.result.taulight.message.types.DialogRequest;

import java.util.UUID;

public class DialogClientChain extends ClientChain {
    public DialogClientChain(IOController io) {
        super(io);
    }

    public synchronized UUID getDialogID(String nickname)
            throws InterruptedException, DeserializationException, SandnodeErrorException,
            UnknownSandnodeErrorException, UnprocessedMessagesException {
        send(new DialogRequest(nickname));

        RawMessage raw = queue.take();

        ServerErrorManager.instance().handleError(raw);

        return new UUIDMessage(raw).uuid;
    }
}
