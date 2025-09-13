package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.key.ServerSource;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.PublicKeyRequest;
import net.result.sandnode.message.types.PublicKeyResponse;
import net.result.sandnode.serverclient.SandnodeClient;

public class PublicKeyClientChain extends ClientChain {
    public PublicKeyClientChain(SandnodeClient client) {
        super(client);
    }

    public AsymmetricKeyStorage getPublicKey() {
        RawMessage response = sendAndReceive(new PublicKeyRequest());
        PublicKeyResponse publicKeyResponse = new PublicKeyResponse(response);
        AsymmetricKeyStorage keyStorage = publicKeyResponse.keyStorage;
        client.node().agent().config.saveServerKey(new ServerSource(client.address), client.address, keyStorage);
        io().setServerKey(keyStorage);
        return keyStorage;
    }
}
