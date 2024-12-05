package net.result.sandnode.messages.types;

import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.exceptions.CannotUseEncryption;
import net.result.sandnode.exceptions.NoSuchEncryptionException;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.Message;
import net.result.sandnode.messages.util.Headers;
import net.result.sandnode.encryption.Encryptions;
import net.result.sandnode.encryption.interfaces.IAsymmetricConvertor;
import net.result.sandnode.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.IAsymmetricKeyStorage;
import org.jetbrains.annotations.NotNull;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static net.result.sandnode.messages.util.MessageTypes.PUB;
import static net.result.sandnode.encryption.Encryption.NONE;

public class PublicKeyResponse extends Message {
    public final IAsymmetricKeyStorage keyStorage;

    public PublicKeyResponse(@NotNull Headers headers, @NotNull IAsymmetricKeyStorage keyStorage) {
        super(headers.set(PUB).set(NONE).set("encryption", "" + keyStorage.encryption().asByte()));
        this.keyStorage = keyStorage;
    }

    public PublicKeyResponse(@NotNull IMessage response) throws NoSuchEncryptionException, CannotUseEncryption,
            CreatingKeyException {
        super(response.getHeaders());
        IAsymmetricEncryption encryption = Encryptions.findAsymmetric(Byte.parseByte(getHeaders().get("encryption")));
        keyStorage = encryption.publicKeyConvertor().toKeyStorage(new String(response.getBody()));
    }

    @Override
    public byte[] getBody() {
        IAsymmetricEncryption encryption = keyStorage.encryption();
        IAsymmetricConvertor convertor = encryption.publicKeyConvertor();
        String string = convertor.toEncodedString(keyStorage);
        return string.getBytes(US_ASCII);
    }
}
