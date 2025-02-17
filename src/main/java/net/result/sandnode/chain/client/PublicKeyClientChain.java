package net.result.sandnode.chain.client;

import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.types.PublicKeyRequest;
import net.result.sandnode.message.types.PublicKeyResponse;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.util.IOController;
import net.result.sandnode.chain.Chain;

public class PublicKeyClientChain extends Chain {
    public PublicKeyClientChain(IOController io) {
        super(io);
    }

    @Override
    public void sync() throws InterruptedException, EncryptionTypeException, NoSuchEncryptionException,
            CreatingKeyException, ExpectedMessageException, SandnodeErrorException,
            UnknownSandnodeErrorException {
        IMessage request = new PublicKeyRequest();
        send(request);

        IMessage response = queue.take();

        if (response.headers().type() == MessageTypes.ERR) {
            ErrorMessage errorMessage = new ErrorMessage(response);
            request.headers().setChainID(getID());
            ServerErrorManager.instance().throwAll(errorMessage.error);
            return;
        }

        PublicKeyResponse publicKeyResponse = new PublicKeyResponse(response);
        io.setServerKey(publicKeyResponse.keyStorage);
    }
}
