package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.encryption.interfaces.SymmetricEncryption;
import net.result.sandnode.encryption.interfaces.SymmetricKeyStorage;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.crypto.KeyNotCreatedException;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.message.types.SymMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.util.IOController;

public class SymKeyClientChain extends ClientChain {
    private final SymmetricEncryption symmetricEncryption;

    public SymKeyClientChain(IOController io, SymmetricEncryption symmetricEncryption) {
        super(io);
        this.symmetricEncryption = symmetricEncryption;
    }

    public void sendSymKey() throws InterruptedException, KeyNotCreatedException, ExpectedMessageException,
            UnprocessedMessagesException {
        SymmetricKeyStorage keyStorage = symmetricEncryption.generate();

        if (!io.keyStorageRegistry.has(io.serverEncryption()))
            throw new KeyNotCreatedException(io.serverEncryption());

        IMessage symMessage = new SymMessage(new Headers().setBodyEncryption(io.serverEncryption()), keyStorage);

        send(symMessage);

        io.setClientKey(keyStorage);

        new HappyMessage(queue.take());
    }
}
