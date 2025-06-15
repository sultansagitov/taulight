package net.result.sandnode.message.types;

import net.result.sandnode.db.KeyStorageEntity;
import net.result.sandnode.dto.KeyDTO;
import net.result.sandnode.encryption.Encryptions;
import net.result.sandnode.encryption.interfaces.Encryption;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.crypto.CannotUseEncryption;
import net.result.sandnode.exception.crypto.CreatingKeyException;
import net.result.sandnode.exception.crypto.EncryptionTypeException;
import net.result.sandnode.exception.crypto.NoSuchEncryptionException;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.interfaces.AsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.message.util.MessageTypes;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class PublicKeyResponse extends Message {
    public final AsymmetricKeyStorage keyStorage;

    public PublicKeyResponse(@NotNull Headers headers, @NotNull AsymmetricKeyStorage keyStorage) {
        super(headers
                .setType(MessageTypes.PUB)
                .setBodyEncryption(Encryptions.NONE)
                .setValue("encryption", "" + keyStorage.encryption().asByte()));
        this.keyStorage = keyStorage;
    }

    public PublicKeyResponse(@NotNull RawMessage response) throws NoSuchEncryptionException, CreatingKeyException,
            EncryptionTypeException, ExpectedMessageException {
        super(response.expect(MessageTypes.PUB).headers());
        byte encryptionByte = Byte.parseByte(headers().getValue("encryption"));
        AsymmetricEncryption encryption = EncryptionManager.findAsymmetric(encryptionByte);
        keyStorage = encryption.publicKeyConvertor().toKeyStorage(new String(response.getBody()));
    }

    @Override
    public byte[] getBody() {
        try {
            return keyStorage.encodedPublicKey().getBytes(StandardCharsets.UTF_8);
        } catch (CannotUseEncryption e) {
            throw new ImpossibleRuntimeException(e);
        }
    }

    public static PublicKeyResponse fromEntity(KeyStorageEntity entity)
            throws CreatingKeyException, EncryptionTypeException {
        Encryption encryption = entity.encryption();
        AsymmetricKeyStorage keyStorage = encryption.asymmetric()
                .publicKeyConvertor()
                .toKeyStorage(entity.encodedKey());

        return new PublicKeyResponse(new Headers().setValue("id", entity.id().toString()), keyStorage);
    }

    public static KeyDTO getKeyDTO(RawMessage raw)
            throws NoSuchEncryptionException, CreatingKeyException, EncryptionTypeException, ExpectedMessageException {
        PublicKeyResponse response = new PublicKeyResponse(raw);
        UUID keyID = UUID.fromString(response.headers().getValue("id"));
        AsymmetricKeyStorage keyStorage = response.keyStorage;
        return new KeyDTO(keyID, keyStorage);
    }
}
