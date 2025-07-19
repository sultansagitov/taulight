package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.encryption.interfaces.SymmetricKeyStorage;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.crypto.KeyNotCreatedException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.message.types.SymMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.serverclient.SandnodeClient;

public class SymKeyClientChain extends ClientChain {
    public SymKeyClientChain(SandnodeClient client) {
        super(client);
    }

    public void sendSymKey() throws InterruptedException, KeyNotCreatedException, ExpectedMessageException,
            UnprocessedMessagesException, UnknownSandnodeErrorException, SandnodeErrorException {
        SymmetricKeyStorage keyStorage = client.config.symmetricKeyEncryption().generate();

        if (!io.keyStorageRegistry.has(io.serverEncryption()))
            throw new KeyNotCreatedException(io.serverEncryption());

        Message symMessage = new SymMessage(new Headers().setBodyEncryption(io.serverEncryption()), keyStorage);

        send(symMessage);

        io.setClientKey(keyStorage);

        new HappyMessage(receive());
    }
}
