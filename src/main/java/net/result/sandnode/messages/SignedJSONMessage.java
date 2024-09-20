package net.result.sandnode.messages;

import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.util.encodings.base64.Base64Encoder;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.interfaces.IEncryptor;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
import net.result.sandnode.util.encryption.rsa.RSAEncryptor;
import net.result.sandnode.util.hashers.IHasher;
import net.result.sandnode.util.hashers.SHA256Hasher;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class SignedJSONMessage extends JSONMessage implements IMessage {
    protected GlobalKeyStorage globalKeyStorage;

    public SignedJSONMessage(@NotNull HeadersBuilder headersBuilder, @NotNull JSONObject content) {
        super(headersBuilder, content);
    }

    public String getSign() throws ReadingKeyException, EncryptionException {
        final IHasher hasher = new SHA256Hasher();
        final IEncryptor encryptor = new RSAEncryptor();

        final String hash = hasher.hash(getContent().toString());
        final IKeyStorage keyStorage = globalKeyStorage.getRSAKeyStorage();
        final byte[] encrypted = encryptor.encrypt(hash, keyStorage);
        return new Base64Encoder().encode(encrypted);
    }

    @Override
    public byte @NotNull [] getBody() throws ReadingKeyException {
        return getContent().toString().getBytes(US_ASCII);
    }
}
