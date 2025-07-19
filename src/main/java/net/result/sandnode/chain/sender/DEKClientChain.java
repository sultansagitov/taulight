package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.dto.DEKDTO;
import net.result.sandnode.dto.KeyDTO;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.crypto.CreatingKeyException;
import net.result.sandnode.exception.crypto.CryptoException;
import net.result.sandnode.exception.crypto.EncryptionTypeException;
import net.result.sandnode.exception.crypto.NoSuchEncryptionException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.UUIDMessage;
import net.result.sandnode.message.types.DEKListMessage;
import net.result.sandnode.message.types.DEKRequest;
import net.result.sandnode.message.types.PublicKeyResponse;
import net.result.sandnode.serverclient.SandnodeClient;

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
        return new UUIDMessage(receive()).uuid;
    }

    public UUID sendDEK(String nickname, UUID encryptorID, KeyStorage keyStorage)
            throws InterruptedException, SandnodeErrorException, UnknownSandnodeErrorException,
            UnprocessedMessagesException, CryptoException, DeserializationException {
        KeyStorage encryptor = client.node.agent().config.loadPersonalKey(client.address, encryptorID);
        return sendDEK(nickname, new KeyDTO(encryptorID, encryptor), keyStorage);
    }

    public Collection<DEKDTO> get() throws UnprocessedMessagesException, InterruptedException,
            UnknownSandnodeErrorException, SandnodeErrorException, ExpectedMessageException, DeserializationException {
        send(DEKRequest.get());
        return new DEKListMessage(receive()).list();
    }

    public KeyDTO getKeyOf(String nickname) throws ExpectedMessageException, InterruptedException,
            UnknownSandnodeErrorException, SandnodeErrorException, UnprocessedMessagesException,
            EncryptionTypeException, NoSuchEncryptionException, CreatingKeyException, StorageException {
        send(DEKRequest.getPersonalKeyOf(nickname));
        KeyDTO key = PublicKeyResponse.getKeyDTO(receive());

        client.node.agent().config.saveEncryptor(client.address, nickname, key.keyID(), key.keyStorage());

        return key;
    }
}
