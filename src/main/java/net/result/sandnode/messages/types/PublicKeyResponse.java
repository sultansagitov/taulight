package net.result.sandnode.messages.types;

import net.result.sandnode.exceptions.*;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.Message;
import net.result.sandnode.messages.util.Headers;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.IAsymmetricKeyStorage;
import org.jetbrains.annotations.NotNull;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static net.result.sandnode.messages.util.MessageTypes.PUB;
import static net.result.sandnode.encryption.Encryption.NONE;

public class PublicKeyResponse extends Message {
    public final IAsymmetricKeyStorage keyStorage;

    public PublicKeyResponse(@NotNull Headers headers, @NotNull IAsymmetricKeyStorage keyStorage) {
        super(headers
                .setType(PUB)
                .setBodyEncryption(NONE)
                .setValue("encryption", "" + keyStorage.encryption().asByte()));
        this.keyStorage = keyStorage;
    }

    public PublicKeyResponse(@NotNull IMessage response)
            throws NoSuchEncryptionException, CreatingKeyException, EncryptionTypeException, ExpectedMessageException {
        super(response.getHeaders());
        ExpectedMessageException.check(response, PUB);
        byte encryptionByte = Byte.parseByte(getHeaders().getValue("encryption"));
        IAsymmetricEncryption encryption = EncryptionManager.findAsymmetric(encryptionByte);
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
