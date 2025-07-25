package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.dto.DEKDTO;
import net.result.sandnode.dto.KeyDTO;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.ProtocolException;
import net.result.sandnode.exception.StorageException;
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
            throws InterruptedException, SandnodeErrorException, ProtocolException, CryptoException {
        var raw = sendAndReceive(DEKRequest.send(nickname, encryptor, keyStorage));
        return new UUIDMessage(raw).uuid;
    }

    public UUID sendDEK(String nickname, UUID encryptorID, KeyStorage keyStorage)
            throws InterruptedException, SandnodeErrorException, ProtocolException, CryptoException {
        var encryptor = client.node().agent().config.loadPersonalKey(client.address, encryptorID);
        return sendDEK(nickname, new KeyDTO(encryptorID, encryptor), keyStorage);
    }

    public Collection<DEKDTO> get() throws ProtocolException, InterruptedException, SandnodeErrorException {
        var raw = sendAndReceive(DEKRequest.get());
        return new DEKListMessage(raw).list();
    }

    public KeyDTO getKeyOf(String nickname) throws ProtocolException, InterruptedException, SandnodeErrorException,
            EncryptionTypeException, NoSuchEncryptionException, CreatingKeyException, StorageException {
        var raw = sendAndReceive(DEKRequest.getPersonalKeyOf(nickname));
        var key = PublicKeyResponse.getKeyDTO(raw);

        client.node().agent().config.saveEncryptor(client.address, nickname, key.keyID(), key.keyStorage());

        return key;
    }
}
