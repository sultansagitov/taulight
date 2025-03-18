package net.result.sandnode.chain.server;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.encryption.interfaces.AsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.crypto.EncryptionTypeException;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.exception.error.ServerSandnodeErrorException;
import net.result.sandnode.message.types.PublicKeyResponse;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.error.Errors;
import net.result.sandnode.serverclient.Session;

public class PublicKeyServerChain extends ServerChain implements ReceiverChain {
    public PublicKeyServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, UnprocessedMessagesException, ServerSandnodeErrorException {
        queue.take();
        AsymmetricEncryption encryption = session.server.serverConfig.mainEncryption();
        AsymmetricKeyStorage asymmetricKeyStorage;
        try {
            asymmetricKeyStorage = session.server.node.keyStorageRegistry.asymmetricNonNull(encryption);
        } catch (KeyStorageNotFoundException e) {
            throw new ServerSandnodeErrorException(e);
        } catch (EncryptionTypeException e) {
            send(Errors.INCORRECT_ENCRYPTION.createMessage());
            return;
        }

        Headers headers = new Headers().setFin(true);
        PublicKeyResponse request = new PublicKeyResponse(headers, asymmetricKeyStorage);
        send(request);
    }
}
