package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.encryption.interfaces.AsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.exception.crypto.EncryptionTypeException;
import net.result.sandnode.exception.error.IncorrectEncryptionException;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.exception.error.ServerErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.PublicKeyResponse;

public class PublicKeyServerChain extends ServerChain implements ReceiverChain {
    @Override
    public PublicKeyResponse handle(RawMessage ignored) {
        AsymmetricEncryption encryption = session.server.serverConfig.mainEncryption();
        AsymmetricKeyStorage asymmetricKeyStorage;
        try {
            asymmetricKeyStorage = session.node().keyStorageRegistry.asymmetricNonNull(encryption);
        } catch (KeyStorageNotFoundException e) {
            throw new ServerErrorException(e);
        } catch (EncryptionTypeException e) {
            throw new IncorrectEncryptionException();
        }

        return new PublicKeyResponse(asymmetricKeyStorage);
    }
}
