package net.result.sandnode.link;

import net.result.sandnode.Agent;
import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.exceptions.InvalidLinkSyntaxException;
import net.result.sandnode.exceptions.KeyStorageNotFoundException;
import net.result.sandnode.exceptions.CannotUseEncryption;
import net.result.sandnode.exceptions.NoSuchEncryptionException;
import net.result.sandnode.server.SandnodeServer;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.util.NetworkUtil;
import net.result.sandnode.encryption.interfaces.IAsymmetricConvertor;
import net.result.sandnode.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.IKeyStorage;
import org.jetbrains.annotations.NotNull;

import java.net.URISyntaxException;
import java.util.Map;

public class Links {
    public static @NotNull String getHubLink(@NotNull SandnodeServer server) throws KeyStorageNotFoundException {
        IAsymmetricEncryption encryption = server.node.nodeConfig.mainEncryption();
        IAsymmetricConvertor convertor = encryption.publicKeyConvertor();
        IKeyStorage keyStorage = server.node.globalKeyStorage.getNonNull(encryption);

        String key = convertor.toEncodedString(keyStorage);
        return String.format(
                "h>>%s>%s>%s",
                NetworkUtil.replaceZeroes(server.serverConfig.endpoint(), 52525),
                encryption.name(),
                key
        );
    }

    public static @NotNull String toString(
            @NotNull Agent agent,
            @NotNull Map<Endpoint, IAsymmetricEncryption> map
    ) throws KeyStorageNotFoundException {
        StringBuilder builder = new StringBuilder("a");

        for (Map.Entry<Endpoint, IAsymmetricEncryption> entry : map.entrySet()) {
            Endpoint endpoint = entry.getKey();
            IAsymmetricEncryption encryption = entry.getValue();

            IAsymmetricConvertor convertor = encryption.publicKeyConvertor();
            IKeyStorage keyStorage = agent.globalKeyStorage.getNonNull(encryption);
            String key = convertor.toEncodedString(keyStorage);

            builder
                    .append(">>")
                    .append(endpoint.toString(52525))
                    .append(">")
                    .append(agent.memberID())
                    .append(">")
                    .append(encryption.name())
                    .append(">")
                    .append(key);
        }

        return builder.toString();
    }

    public static @NotNull LinkInfo fromString(@NotNull String string) throws CreatingKeyException, InvalidLinkSyntaxException,
            NoSuchEncryptionException, CannotUseEncryption, URISyntaxException {
        String clean = string.replaceAll("[\\s\\n\\t]+", "");
        char c = clean.charAt(0);
        String substring = clean.substring(3);
        return switch (c) {
            case 'h' -> HubInfo.parse(substring);
            case 'a' -> AgentInfo.parse(substring);
            default ->
                    throw new InvalidLinkSyntaxException(String.format("Unknown link symbol \"%s\", should be 'h', 'a' or 'm'", c));
        };
    }
}
