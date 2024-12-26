package net.result.sandnode.chain.server;

import net.result.sandnode.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.IAsymmetricKeyStorage;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.messages.types.PublicKeyResponse;
import net.result.sandnode.messages.util.Headers;
import net.result.sandnode.server.Session;

import static net.result.sandnode.server.ServerError.INCORRECT_ENCRYPTION;
import static net.result.sandnode.server.ServerError.SERVER_ERROR;

public class PublicKeyServerChain extends ServerChain {
    public PublicKeyServerChain(Session session) {
        super(session);
    }

    @Override
    public void start() throws InterruptedException {
        queue.take();
        IAsymmetricEncryption encryption = session.server.serverConfig.mainEncryption();
        Headers headers = new Headers().setFin(true).setChainID(getID());
        IAsymmetricKeyStorage asymmetricKeyStorage;
        try {
            asymmetricKeyStorage = session.server.node.globalKeyStorage.getAsymmetricNonNull(encryption);
        } catch (KeyStorageNotFoundException e) {
            send(SERVER_ERROR.message());
            return;
        } catch (EncryptionTypeException e) {
            send(INCORRECT_ENCRYPTION.message());
            return;
        }

        PublicKeyResponse request = new PublicKeyResponse(headers, asymmetricKeyStorage);
        send(request);
    }
}
