package net.result.sandnode.link;

import net.result.sandnode.Hub;
import net.result.sandnode.config.IServerConfig;
import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.exceptions.KeyStorageNotFoundException;
import net.result.sandnode.exceptions.CannotUseEncryption;
import net.result.sandnode.exceptions.NoSuchEncryptionException;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.encryption.Encryptions;
import net.result.sandnode.encryption.interfaces.IAsymmetricConvertor;
import net.result.sandnode.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.IAsymmetricKeyStorage;
import net.result.sandnode.encryption.interfaces.IKeyStorage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.net.URISyntaxException;

public class HubInfo implements ServerLinkInfo {

    public final Endpoint endpoint;
    public final IAsymmetricKeyStorage keyStorage;

    private HubInfo(Endpoint endpoint, IAsymmetricKeyStorage keyStorage) {
        this.endpoint = endpoint;
        this.keyStorage = keyStorage;
    }

    public static @NotNull String getLink(
            @NotNull Hub hub,
            @NotNull IServerConfig serverConfig
    ) throws KeyStorageNotFoundException {
        IAsymmetricEncryption encryption = hub.nodeConfig.mainEncryption();
        IAsymmetricConvertor publicConvertor = encryption.publicKeyConvertor();
        IKeyStorage keyStorage = hub.globalKeyStorage.getNonNull(encryption);
        String encodedString = publicConvertor.toEncodedString(keyStorage);
        return String.format("h>>%s>%s>%s", serverConfig.endpoint().toString(52525), encryption.name(), encodedString);
    }

    @Contract("_ -> new")
    public static @NotNull HubInfo parse(@NotNull String string) throws CreatingKeyException, NoSuchEncryptionException,
            CannotUseEncryption, URISyntaxException {
        String[] split = string.split(">");
        Endpoint endpoint;
        endpoint = Endpoint.getFromString(split[0], 52525);

        String encryptionName = split[1];
        IAsymmetricEncryption encryption;
        encryption = Encryptions.findAsymmetric(encryptionName);

        IAsymmetricConvertor convertor = encryption.publicKeyConvertor();
        IAsymmetricKeyStorage keyStorage = convertor.toKeyStorage(split[2]);

        return new HubInfo(endpoint, keyStorage);
    }

    @Override
    public Endpoint endpoint() {
        return endpoint;
    }

    @Override
    public IAsymmetricKeyStorage keyStorage() {
        return keyStorage;
    }
}
