package net.result.sandnode.link;

import net.result.sandnode.Agent;
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

public record AgentInfo(Endpoint endpoint, String memberID, IAsymmetricKeyStorage keyStorage) implements ServerLinkInfo {
    public static @NotNull String getLink(
            @NotNull Agent agent,
            @NotNull String memberID,
            @NotNull IServerConfig serverConfig
    ) throws KeyStorageNotFoundException {
        IKeyStorage keyStorage = agent.globalKeyStorage.getNonNull(agent.nodeConfig.mainEncryption());
        IAsymmetricEncryption encryption = (IAsymmetricEncryption) keyStorage.encryption();
        IAsymmetricConvertor publicConvertor = encryption.publicKeyConvertor();
        String encodedString = publicConvertor.toEncodedString(keyStorage);
        return String.format("a>>%s>%s>%s>%s", serverConfig.endpoint(), memberID, encryption.name(), encodedString);
    }

    @Contract("_ -> new")
    public static @NotNull LinkInfo parse(@NotNull String string) throws CreatingKeyException,
            NoSuchEncryptionException, CannotUseEncryption, URISyntaxException {
        String[] split = string.split(">");
        Endpoint endpoint;
        endpoint = Endpoint.getFromString(split[0], 52525);

        String memberID = split[1];

        String encryptionName = split[2];
        IAsymmetricEncryption encryption;
        encryption = Encryptions.findAsymmetric(encryptionName);


        IAsymmetricConvertor convertor = encryption.publicKeyConvertor();
        IAsymmetricKeyStorage keyStorage = convertor.toKeyStorage(split[3]);

        return new AgentInfo(endpoint, memberID, keyStorage);
    }
}
