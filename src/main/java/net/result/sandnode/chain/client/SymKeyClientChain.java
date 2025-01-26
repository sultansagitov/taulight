package net.result.sandnode.chain.client;

import net.result.sandnode.encryption.interfaces.ISymmetricEncryption;
import net.result.sandnode.encryption.interfaces.ISymmetricKeyStorage;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.KeyNotCreatedException;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.message.types.SymMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.util.IOController;
import net.result.sandnode.chain.Chain;

public class SymKeyClientChain extends Chain {
    private final ISymmetricEncryption symmetricEncryption;

    public SymKeyClientChain(IOController io, ISymmetricEncryption symmetricEncryption) {
        super(io);
        this.symmetricEncryption = symmetricEncryption;
    }

    @Override
    public void sync() throws InterruptedException, KeyNotCreatedException, ExpectedMessageException {
        ISymmetricKeyStorage keyStorage = symmetricEncryption.generate();

        if (!io.globalKeyStorage.has(io.getServerEncryption()))
            throw new KeyNotCreatedException(io.getServerEncryption());

        IMessage symMessage = new SymMessage(new Headers().setBodyEncryption(io.getServerEncryption()), keyStorage);

        send(symMessage);

        io.setClientKey(keyStorage);

        new HappyMessage(queue.take());
    }
}
