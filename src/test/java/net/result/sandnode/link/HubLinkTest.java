package net.result.sandnode.link;

import net.result.sandnode.Hub;
import net.result.sandnode.config.HubConfig;
import net.result.sandnode.config.IHubConfig;
import net.result.sandnode.config.ServerConfig;
import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.exceptions.InvalidLinkSyntaxException;
import net.result.sandnode.exceptions.KeyStorageNotFoundException;
import net.result.sandnode.exceptions.CannotUseEncryption;
import net.result.sandnode.exceptions.NoSuchEncryptionException;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.server.Session;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.encryption.Encryptions;
import net.result.sandnode.encryption.GlobalKeyStorage;
import net.result.sandnode.encryption.interfaces.IAsymmetricConvertor;
import net.result.sandnode.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.IKeyStorage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.List;

import static net.result.sandnode.encryption.SymmetricEncryption.AES;

class HubLinkTest {
    @Test
    void parse() throws CreatingKeyException, InvalidLinkSyntaxException, KeyStorageNotFoundException, NoSuchEncryptionException,
            CannotUseEncryption, URISyntaxException {
        for (IAsymmetricEncryption a : Encryptions.getAsymmetric()) {
            for (String host : List.of("127.0.0.1", "mydomain.com", "[::1]")) {
                extracted(a, host, 52525);
                extracted(a, host, 500);
            }
        }
    }

    private static void extracted(@NotNull IAsymmetricEncryption a, String host, int port) throws CreatingKeyException,
            InvalidLinkSyntaxException, KeyStorageNotFoundException, NoSuchEncryptionException, CannotUseEncryption,
            URISyntaxException {
        IKeyStorage keyStorage = a.generator().generate();
        GlobalKeyStorage globalKeyStorage = new GlobalKeyStorage().set(keyStorage);

        IHubConfig hubConfig = new HubConfig(a, AES);
        String link = HubInfo.getLink(
                new CustomHub(globalKeyStorage, hubConfig),
                new ServerConfig(new Endpoint(host, port), null, null)
        );

        HubInfo hubInfo = (HubInfo) Links.fromString(link);

        Assertions.assertEquals(hubInfo.endpoint().host(), host);
        Assertions.assertEquals(hubInfo.endpoint().port(), port);
        Assertions.assertEquals(hubInfo.keyStorage().encryption(), a);

        IAsymmetricConvertor convertor = a.publicKeyConvertor();
        String s1 = convertor.toEncodedString(hubInfo.keyStorage());
        String s2 = convertor.toEncodedString(keyStorage);

        Assertions.assertEquals(s1, s2);
    }

    private static class CustomHub extends Hub {
        public CustomHub(GlobalKeyStorage globalKeyStorage, IHubConfig hubConfig) {
            super(globalKeyStorage, hubConfig);
        }

        @Override
        public void onAgentMessage(@NotNull IMessage request, @NotNull Session session) {}
    }
}