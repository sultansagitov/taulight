package net.result.sandnode.link;

import net.result.sandnode.User;
import net.result.sandnode.config.IUserConfig;
import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.exceptions.InvalidLinkSyntax;
import net.result.sandnode.exceptions.KeyStorageNotFoundException;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.server.Session;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.util.encryption.AsymmetricEncryption;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.interfaces.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.List;

import static net.result.sandnode.util.encryption.AsymmetricEncryption.RSA;

class UserInfoTest {

    private static void extracted(AsymmetricEncryption a, String host, int port) throws ReadingKeyException,
            CreatingKeyException, InvalidLinkSyntax, KeyStorageNotFoundException, NoSuchEncryptionException,
            CannotUseEncryption, URISyntaxException {
        IKeyStorage keyStorage = a.generator().generate();
        GlobalKeyStorage globalKeyStorage = new GlobalKeyStorage().set(keyStorage);

        IUserConfig userConfig = new IUserConfig() {
            @Override
            public @NotNull IAsymmetricEncryption getMainEncryption() {
                return a;
            }

            @Override
            public ISymmetricEncryption getSymmetricKeyEncryption() {
                return null;
            }

            @Override
            public void addKey(@NotNull Endpoint endpoint, @NotNull IKeyStorage keyStorage) {
            }

            @Override
            public void saveKeysJSON() {
            }

            @Override
            public @Nullable IAsymmetricKeyStorage getPublicKey(@NotNull Endpoint endpoint) {
                return null;
            }
        };
        String link = UserInfo.getLink(
                new User(globalKeyStorage, userConfig) {
                    @Override
                    public void onUserMessage(@NotNull IMessage request, @NotNull Session session) {
                    }
                },
                "myname",
                () -> new Endpoint(host, port)
        );

        UserInfo hubInfo = (UserInfo) Links.fromString(link);

        Assertions.assertEquals(hubInfo.getEndpoint().host, host);
        Assertions.assertEquals(hubInfo.getEndpoint().port, port);
        Assertions.assertEquals(hubInfo.getKeyStorage().encryption(), a);

        IAsymmetricConvertor convertor = a.publicKeyConvertor();
        String s1 = convertor.toEncodedString(hubInfo.getKeyStorage());
        String s2 = convertor.toEncodedString(keyStorage);

        Assertions.assertEquals(s1, s2);
    }

    @Test
    void parse() throws ReadingKeyException, CreatingKeyException, InvalidLinkSyntax, KeyStorageNotFoundException,
            NoSuchEncryptionException, CannotUseEncryption, URISyntaxException {
        for (AsymmetricEncryption a : List.of(RSA)) {
            for (String host : List.of("127.0.0.1", "mydomain.com", "[::1]")) {
                extracted(a, host, 52525);
                extracted(a, host, 500);
            }
        }
    }
}