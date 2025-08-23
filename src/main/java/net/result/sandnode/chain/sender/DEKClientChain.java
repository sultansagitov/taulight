package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.dto.DEKResponseDTO;
import net.result.sandnode.dto.KeyDTO;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.StorageException;
import net.result.sandnode.exception.crypto.*;
import net.result.sandnode.message.UUIDMessage;
import net.result.sandnode.message.types.DEKListMessage;
import net.result.sandnode.message.types.DEKRequest;
import net.result.sandnode.message.types.PublicKeyResponse;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.util.DEKUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class DEKClientChain extends ClientChain {
    public DEKClientChain(SandnodeClient client) {
        super(client);
    }

    public UUID sendDEK(String receiver, KeyStorage encryptor, KeyStorage dek) {
        var raw = sendAndReceive(DEKRequest.send(receiver, encryptor, dek));
        var keyID = new UUIDMessage(raw).uuid;

        client.node().agent().config.saveDEK(client.address, receiver, keyID, dek);

        return keyID;
    }

    public Collection<DEKResponseDTO> get() {
        var raw = sendAndReceive(DEKRequest.get());
        List<DEKResponseDTO> keys = new DEKListMessage(raw).list();

        List<Exception> exceptions = new ArrayList<>();

        for (DEKResponseDTO key : keys) {
            try {
                var agent = client.node().agent();
                var personalKey = agent.config.loadPersonalKey(client.address, client.nickname);
                var decrypted = DEKUtil.decrypt(key.dek.encryptedKey, personalKey);
                agent.config.saveDEK(client.address, key.senderNickname, key.dek.id, decrypted);
            } catch (EncryptionTypeException | NoSuchEncryptionException | CreatingKeyException |
                     WrongKeyException | CannotUseEncryption | PrivateKeyNotFoundException | StorageException e) {
                exceptions.add(e);
            }
        }

        if (!exceptions.isEmpty()) {
            RuntimeException aggregated = new RuntimeException("Errors occurred during DEK processing");
            for (Exception e : exceptions) {
                aggregated.addSuppressed(e);
            }
            throw aggregated;
        }

        return keys;
    }

    public KeyDTO getKeyOf(String nickname) {
        var raw = sendAndReceive(DEKRequest.getPersonalKeyOf(nickname));
        var key = PublicKeyResponse.getKeyDTO(raw);

        client.node().agent().config.saveEncryptor(client.address, nickname, key.keyStorage());

        return key;
    }
}
