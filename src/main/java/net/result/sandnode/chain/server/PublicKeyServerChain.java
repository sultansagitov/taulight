package net.result.sandnode.chain.server;

import net.result.sandnode.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.IAsymmetricKeyStorage;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.messages.types.PublicKeyResponse;
import net.result.sandnode.messages.util.Headers;
import net.result.sandnode.server.Errors;
import net.result.sandnode.server.Session;

public class PublicKeyServerChain extends ServerChain {
    public PublicKeyServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException {
        queue.take();
        IAsymmetricEncryption encryption = session.server.serverConfig.mainEncryption();
        IAsymmetricKeyStorage asymmetricKeyStorage;
        try {
            asymmetricKeyStorage = session.server.node.globalKeyStorage.getAsymmetricNonNull(encryption);
        } catch (KeyStorageNotFoundException e) {
            send(Errors.SERVER_ERROR.message());
            return;
        } catch (EncryptionTypeException e) {
            send(Errors.INCORRECT_ENCRYPTION.message());
            return;
        }

        Headers headers = new Headers().setFin(true);
        PublicKeyResponse request = new PublicKeyResponse(headers, asymmetricKeyStorage);
        send(request);
    }
}
