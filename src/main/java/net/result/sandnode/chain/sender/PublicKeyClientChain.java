package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.crypto.CryptoException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.PublicKeyRequest;
import net.result.sandnode.message.types.PublicKeyResponse;
import net.result.sandnode.serverclient.SandnodeClient;

public class PublicKeyClientChain extends ClientChain {
    public PublicKeyClientChain(SandnodeClient client) {
        super(client);
    }

    public void getPublicKey() throws InterruptedException, ProtocolException, SandnodeErrorException, CryptoException {
        RawMessage response = sendAndReceive(new PublicKeyRequest());
        PublicKeyResponse publicKeyResponse = new PublicKeyResponse(response);
        io.setServerKey(publicKeyResponse.keyStorage);
    }
}
