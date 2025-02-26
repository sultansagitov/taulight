package net.result.sandnode.message.types;

import net.result.sandnode.encryption.Encryptions;
import net.result.sandnode.exception.*;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.interfaces.AsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.message.util.MessageTypes;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public class PublicKeyResponse extends Message {
    public final AsymmetricKeyStorage keyStorage;

    public PublicKeyResponse(@NotNull Headers headers, @NotNull AsymmetricKeyStorage keyStorage) {
        super(headers
                .setType(MessageTypes.PUB)
                .setBodyEncryption(Encryptions.NONE)
                .setValue("encryption", "" + keyStorage.encryption().asByte()));
        this.keyStorage = keyStorage;
    }

    public PublicKeyResponse(@NotNull IMessage response) throws NoSuchEncryptionException, CreatingKeyException,
            EncryptionTypeException, ExpectedMessageException {
        super(response.expect(MessageTypes.PUB).headers());
        byte encryptionByte = Byte.parseByte(headers().getValue("encryption"));
        AsymmetricEncryption encryption = EncryptionManager.findAsymmetric(encryptionByte);
        keyStorage = encryption.publicKeyConvertor().toKeyStorage(new String(response.getBody()));
    }

    @Override
    public byte[] getBody() {
        try {
            return keyStorage.encodedPublicKey().getBytes(StandardCharsets.US_ASCII);
        } catch (CannotUseEncryption e) {
            throw new ImpossibleRuntimeException(e);
        }
    }
}
