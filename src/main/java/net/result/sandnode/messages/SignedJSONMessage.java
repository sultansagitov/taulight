package net.result.sandnode.messages;

import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.interfaces.IEncryptor;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
import net.result.sandnode.util.encryption.rsa.RSAEncryptor;
import net.result.sandnode.util.hashers.IHasher;
import net.result.sandnode.util.hashers.SHA256Hasher;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.Base64;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class SignedJSONMessage extends JSONMessage implements IMessage {
    protected GlobalKeyStorage globalKeyStorage;

    public SignedJSONMessage(@NotNull HeadersBuilder headersBuilder, @NotNull JSONObject content) {
        super(headersBuilder, content);
    }

    public String getSign() throws ReadingKeyException, EncryptionException {
        final IHasher hasher = SHA256Hasher.getInstance();
        final IEncryptor encryptor = RSAEncryptor.getInstance();

        final String hash = hasher.hash(getContent().toString());
        final IKeyStorage keyStorage = globalKeyStorage.getRSAKeyStorage();
        final byte[] encrypted = encryptor.encrypt(hash, keyStorage);
        return Base64.getEncoder().encodeToString(encrypted);
    }

    @Override
    public byte @NotNull [] getBody() throws ReadingKeyException {
        return getContent().toString().getBytes(US_ASCII);
    }
}
