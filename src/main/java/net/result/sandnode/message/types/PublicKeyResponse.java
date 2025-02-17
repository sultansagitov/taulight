package net.result.sandnode.message.types;

import net.result.sandnode.exception.*;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.interfaces.AsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import org.jetbrains.annotations.NotNull;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static net.result.sandnode.message.util.MessageTypes.PUB;
import static net.result.sandnode.encryption.Encryptions.NONE;

public class PublicKeyResponse extends Message {
    public final AsymmetricKeyStorage keyStorage;

    public PublicKeyResponse(@NotNull Headers headers, @NotNull AsymmetricKeyStorage keyStorage) {
        super(headers
                .setType(PUB)
                .setBodyEncryption(NONE)
                .setValue("encryption", "" + keyStorage.encryption().asByte()));
        this.keyStorage = keyStorage;
    }

    public PublicKeyResponse(@NotNull IMessage response)
            throws NoSuchEncryptionException, CreatingKeyException, EncryptionTypeException, ExpectedMessageException {
        super(response.expect(PUB).headers());
        byte encryptionByte = Byte.parseByte(headers().getValue("encryption"));
        AsymmetricEncryption encryption = EncryptionManager.findAsymmetric(encryptionByte);
        keyStorage = encryption.publicKeyConvertor().toKeyStorage(new String(response.getBody()));
    }

    @Override
    public byte[] getBody() {
        try {
            return keyStorage.encodedPublicKey().getBytes(US_ASCII);
        } catch (CannotUseEncryption e) {
            throw new ImpossibleRuntimeException(e);
        }
    }
}
