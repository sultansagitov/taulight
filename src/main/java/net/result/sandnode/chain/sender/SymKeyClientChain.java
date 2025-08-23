package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.encryption.interfaces.Encryption;
import net.result.sandnode.encryption.interfaces.SymmetricKeyStorage;
import net.result.sandnode.exception.crypto.KeyNotCreatedException;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.message.types.SymMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.serverclient.SandnodeClient;

public class SymKeyClientChain extends ClientChain {
    public SymKeyClientChain(SandnodeClient client) {
        super(client);
    }

    public void sendSymKey() {
        SymmetricKeyStorage keyStorage = client.config.symmetricKeyEncryption().generate();

        Encryption serverEncryption = io().serverEncryption();
        if (!io().keyStorageRegistry.has(serverEncryption))
            throw new KeyNotCreatedException(serverEncryption);

        Message symMessage = new SymMessage(new Headers().setBodyEncryption(serverEncryption), keyStorage);

        send(symMessage);

        io().setClientKey(keyStorage);

        new HappyMessage(receive());
    }
}
