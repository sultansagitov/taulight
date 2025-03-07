package net.result.sandnode.chain.client;

import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.crypto.CryptoException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.types.PublicKeyRequest;
import net.result.sandnode.message.types.PublicKeyResponse;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.util.IOController;

public class PublicKeyClientChain extends ClientChain {
    public PublicKeyClientChain(IOController io) {
        super(io);
    }

    public void getPublicKey() throws InterruptedException, ExpectedMessageException, SandnodeErrorException,
            UnknownSandnodeErrorException, CryptoException, UnprocessedMessagesException {
        send(new PublicKeyRequest());

        RawMessage response = queue.take();

        if (response.headers().type() == MessageTypes.ERR) {
            ErrorMessage errorMessage = new ErrorMessage(response);
            ServerErrorManager.instance().throwAll(errorMessage.error);
            return;
        }

        PublicKeyResponse publicKeyResponse = new PublicKeyResponse(response);
        io.setServerKey(publicKeyResponse.keyStorage);
    }
}
