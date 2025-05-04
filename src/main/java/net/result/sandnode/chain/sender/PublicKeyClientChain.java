package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.crypto.CryptoException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.PublicKeyRequest;
import net.result.sandnode.message.types.PublicKeyResponse;
import net.result.sandnode.util.IOController;

public class PublicKeyClientChain extends ClientChain {
    public PublicKeyClientChain(IOController io) {
        super(io);
    }

    public void getPublicKey() throws InterruptedException, ExpectedMessageException, SandnodeErrorException,
            UnknownSandnodeErrorException, CryptoException, UnprocessedMessagesException {
        send(new PublicKeyRequest());

        RawMessage response = queue.take();

        ServerErrorManager.instance().handleError(response);

        PublicKeyResponse publicKeyResponse = new PublicKeyResponse(response);
        io.setServerKey(publicKeyResponse.keyStorage);
    }
}
