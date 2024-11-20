package net.result.sandnode.link;

import net.result.sandnode.Hub;
import net.result.sandnode.User;
import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.exceptions.InvalidLinkSyntax;
import net.result.sandnode.exceptions.KeyStorageNotFoundException;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.util.NetworkUtil;
import net.result.sandnode.util.encryption.interfaces.IAsymmetricConvertor;
import net.result.sandnode.util.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
import org.jetbrains.annotations.NotNull;

import java.net.URISyntaxException;
import java.util.Map;

public class Links {
    public static @NotNull String toString(
            @NotNull Hub hub,
            @NotNull Endpoint endpoint,
            @NotNull IAsymmetricEncryption encryption
    ) throws ReadingKeyException, KeyStorageNotFoundException {
        IAsymmetricConvertor convertor = encryption.publicKeyConvertor();
        IKeyStorage keyStorage = hub.globalKeyStorage.getNonNull(encryption);

        String key = convertor.toEncodedString(keyStorage);
        return String.format(
                "h>>%s>%s>%s",
                NetworkUtil.replaceZeroes(endpoint, 52525),
                encryption.name(),
                key
        );
    }

    public static @NotNull String toString(
            @NotNull User user,
            @NotNull Map<Endpoint, IAsymmetricEncryption> map
    ) throws ReadingKeyException, KeyStorageNotFoundException {
        StringBuilder builder = new StringBuilder("u");

        for (Map.Entry<Endpoint, IAsymmetricEncryption> entry : map.entrySet()) {
            Endpoint endpoint = entry.getKey();
            IAsymmetricEncryption encryption = entry.getValue();

            IAsymmetricConvertor convertor = encryption.publicKeyConvertor();
            IKeyStorage keyStorage = user.globalKeyStorage.getNonNull(encryption);
            String key = convertor.toEncodedString(keyStorage);

            builder
                    .append(">>")
                    .append(endpoint.toString(52525))
                    .append(">")
                    .append(user.userID())
                    .append(">")
                    .append(encryption.name())
                    .append(">")
                    .append(key);
        }

        return builder.toString();
    }

    public static @NotNull LinkInfo fromString(@NotNull String string) throws CreatingKeyException, InvalidLinkSyntax,
            NoSuchEncryptionException, CannotUseEncryption, URISyntaxException {
        String clean = string.replaceAll("[\\s\\n\\t]+", "");
        char c = clean.charAt(0);
        String substring = clean.substring(3);
        return switch (c) {
            case 'h' -> HubInfo.parse(substring);
            case 'u' -> UserInfo.parse(substring);
            default ->
                    throw new InvalidLinkSyntax(String.format("Unknown link symbol \"%s\", should be 'h', 'u' or 'm'", c));
        };
    }
}
