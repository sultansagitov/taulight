package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.crypto.CreatingKeyException;
import net.result.sandnode.exception.crypto.CryptoException;
import net.result.sandnode.exception.crypto.EncryptionTypeException;
import net.result.sandnode.exception.crypto.NoSuchEncryptionException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.UUIDMessage;
import net.result.sandnode.message.types.DEKListMessage;
import net.result.sandnode.message.types.PublicKeyResponse;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.dto.KeyDTO;
import net.result.taulight.dto.DEKDTO;
import net.result.taulight.message.types.DEKRequest;

import java.util.Collection;
import java.util.UUID;

public class DEKClientChain extends ClientChain {
    public DEKClientChain(SandnodeClient client) {
        super(client);
    }

    public UUID sendDEK(String nickname, KeyDTO encryptor, KeyStorage keyStorage)
            throws InterruptedException, SandnodeErrorException, UnknownSandnodeErrorException,
            UnprocessedMessagesException, CryptoException, DeserializationException {
        send(DEKRequest.send(nickname, encryptor, keyStorage));

        RawMessage raw = queue.take();
        ServerErrorManager.instance().handleError(raw);

        return new UUIDMessage(raw).uuid;
    }

    public UUID sendDEK(String nickname, UUID encryptorID, KeyStorage keyStorage)
            throws InterruptedException, SandnodeErrorException, UnknownSandnodeErrorException,
            UnprocessedMessagesException, CryptoException, DeserializationException {
        KeyStorage encryptor = client.clientConfig.loadPersonalKey(encryptorID);
        return sendDEK(nickname, new KeyDTO(encryptorID, encryptor), keyStorage);
    }

    public Collection<DEKDTO> get() throws UnprocessedMessagesException, InterruptedException,
            UnknownSandnodeErrorException, SandnodeErrorException, ExpectedMessageException, DeserializationException {
        send(DEKRequest.get());

        RawMessage raw = queue.take();
        ServerErrorManager.instance().handleError(raw);

        return new DEKListMessage(raw).list();
    }

    public KeyDTO getKeyOf(String nickname) throws ExpectedMessageException, DeserializationException,
            InterruptedException, UnknownSandnodeErrorException, SandnodeErrorException, UnprocessedMessagesException,
            EncryptionTypeException, NoSuchEncryptionException, CreatingKeyException, FSException {
        send(DEKRequest.getPersonalKeyOf(nickname));

        RawMessage raw = queue.take();
        ServerErrorManager.instance().handleError(raw);

        // TODO move logic
        PublicKeyResponse response = new PublicKeyResponse(raw);
        UUID keyID = UUID.fromString(response.headers().getValue("id"));
        AsymmetricKeyStorage keyStorage = response.keyStorage;

        KeyDTO key = new KeyDTO(keyID, keyStorage);
        client.clientConfig.saveEncryptor(nickname, key.keyID(), key.keyStorage());

        return key;
    }
}
