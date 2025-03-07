package net.result.taulight.chain.client;

import net.result.sandnode.chain.client.ClientChain;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.util.IOController;
import net.result.taulight.message.types.UUIDMessage;

import java.util.UUID;

public class DialogClientChain extends ClientChain {
    public DialogClientChain(IOController io) {
        super(io);
    }

    public synchronized UUID getDialogID(String memberID)
            throws InterruptedException, ExpectedMessageException, DeserializationException, SandnodeErrorException,
            UnknownSandnodeErrorException, UnprocessedMessagesException {
        send(new DialogRequest(memberID));

        RawMessage raw = queue.take();

        if (raw.headers().type() == MessageTypes.ERR) {
            ErrorMessage errorMessage = new ErrorMessage(raw);
            ServerErrorManager.instance().throwAll(errorMessage.error);
        }

        return new UUIDMessage(raw).uuid;
    }
}
