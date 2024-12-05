package net.result.sandnode.link;

import net.result.sandnode.Agent;
import net.result.sandnode.config.IAgentConfig;
import net.result.sandnode.config.ServerConfig;
import net.result.sandnode.config.AgentConfig;
import net.result.sandnode.encryption.interfaces.IAsymmetricConvertor;
import net.result.sandnode.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.IKeyStorage;
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
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.List;

import static net.result.sandnode.encryption.SymmetricEncryption.AES;

class AgentInfoTest {

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

        IAgentConfig agentConfig = new AgentConfig(a, AES);
        String link = AgentInfo.getLink(
                new CustomAgent(globalKeyStorage, agentConfig),
                "myname",
                new ServerConfig(new Endpoint(host, port), null, null)
        );

        AgentInfo hubInfo = (AgentInfo) Links.fromString(link);

        Assertions.assertEquals(hubInfo.endpoint().host(), host);
        Assertions.assertEquals(hubInfo.endpoint().port(), port);
        Assertions.assertEquals(hubInfo.keyStorage().encryption(), a);

        IAsymmetricConvertor convertor = a.publicKeyConvertor();
        String s1 = convertor.toEncodedString(hubInfo.keyStorage());
        String s2 = convertor.toEncodedString(keyStorage);

        Assertions.assertEquals(s1, s2);
    }

    private static class CustomAgent extends Agent {
        public CustomAgent(GlobalKeyStorage globalKeyStorage, IAgentConfig agentConfig) {
            super(globalKeyStorage, agentConfig);
        }

        @Override
        public void onAgentMessage(@NotNull IMessage request, @NotNull Session session) {
        }
    }
}