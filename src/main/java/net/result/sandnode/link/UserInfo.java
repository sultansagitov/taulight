package net.result.sandnode.link;

import net.result.sandnode.User;
import net.result.sandnode.config.IServerConfig;
import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.exceptions.KeyStorageNotFoundException;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.util.encryption.Encryptions;
import net.result.sandnode.util.encryption.interfaces.IAsymmetricConvertor;
import net.result.sandnode.util.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.util.encryption.interfaces.IAsymmetricKeyStorage;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.net.URISyntaxException;

public class UserInfo implements ServerLinkInfo {
    private final Endpoint endpoint;
    private final String userID;
    private final IKeyStorage keyStorage;

    public UserInfo(@NotNull Endpoint endpoint, String userID, @NotNull IKeyStorage keyStorage) {
        this.endpoint = endpoint;
        this.userID = userID;
        this.keyStorage = keyStorage;
    }

    public static @NotNull String getLink(
            @NotNull User user,
            @NotNull String userID,
            @NotNull IServerConfig serverConfig
    ) throws ReadingKeyException, KeyStorageNotFoundException {
        IKeyStorage keyStorage = user.globalKeyStorage.getNonNull(user.config.getMainEncryption());
        IAsymmetricEncryption encryption = (IAsymmetricEncryption) keyStorage.encryption();
        IAsymmetricConvertor publicConvertor = encryption.publicKeyConvertor();
        String encodedString = publicConvertor.toEncodedString(keyStorage);
        return String.format("u>>%s>%s>%s>%s", serverConfig.getEndpoint(), userID, encryption.name(), encodedString);
    }

    @Contract("_ -> new")
    public static @NotNull LinkInfo parse(@NotNull String string) throws CreatingKeyException,
            NoSuchEncryptionException, CannotUseEncryption, URISyntaxException {
        String[] split = string.split(">");
        Endpoint endpoint;
        endpoint = Endpoint.getFromString(split[0], 52525);

        String userID = split[1];

        String encryptionName = split[2];
        IAsymmetricEncryption encryption;
        encryption = Encryptions.findAsymmetric(encryptionName);


        IAsymmetricConvertor convertor = encryption.publicKeyConvertor();
        IAsymmetricKeyStorage keyStorage = convertor.toKeyStorage(split[3]);

        return new UserInfo(endpoint, userID, keyStorage);
    }

    @Override
    public Endpoint getEndpoint() {
        return endpoint;
    }

    @Override
    public IKeyStorage getKeyStorage() {
        return keyStorage;
    }

    public String getUserID() {
        return userID;
    }
}
