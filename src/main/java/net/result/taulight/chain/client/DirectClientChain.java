package net.result.taulight.chain.client;

import net.result.sandnode.chain.client.ClientChain;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.util.IOController;
import net.result.taulight.message.types.DirectResponse;

import java.util.UUID;

public class DirectClientChain extends ClientChain {
    private final String memberID;
    public UUID chatID;

    public DirectClientChain(IOController io, String memberID) {
        super(io);
        this.memberID = memberID;
    }

    @Override
    public void sync() throws InterruptedException, ExpectedMessageException, DeserializationException,
            SandnodeErrorException, UnknownSandnodeErrorException {
        send(new DirectRequest(memberID));

        RawMessage raw = queue.take();

        if (raw.headers().type() == MessageTypes.ERR) {
            ErrorMessage errorMessage = new ErrorMessage(raw);
            ServerErrorManager.instance().throwAll(errorMessage.error);
        }

        DirectResponse response = new DirectResponse(raw);
        chatID = response.getChatID();
    }
}
