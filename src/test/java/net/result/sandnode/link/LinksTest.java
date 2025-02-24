package net.result.sandnode.link;

import net.result.sandnode.chain.server.ServerChainManager;
import net.result.sandnode.config.ServerConfig;
import net.result.sandnode.encryption.AsymmetricEncryptions;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.KeyStorageRegistry;
import net.result.sandnode.encryption.interfaces.AsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.exception.*;
import net.result.sandnode.hubagent.Hub;
import net.result.sandnode.serverclient.SandnodeServer;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.db.Database;
import net.result.sandnode.group.GroupManager;
import net.result.sandnode.tokens.Tokenizer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class LinksTest {

    @Test
    public void testGetServerLink() throws KeyStorageNotFoundException, EncryptionTypeException {
        EncryptionManager.registerAll();

        SandnodeServer server = createTestServer();

        URI serverLink = Links.getServerLink(server);

        assertNotNull(serverLink);
        assertEquals("sandnode", serverLink.getScheme());
        assertTrue(serverLink.getHost().contains("localhost"));
        assertTrue(serverLink.getQuery().contains("encryption"));
        assertTrue(serverLink.getQuery().contains("key"));
    }

    @Test
    public void testParse() throws CreatingKeyException, InvalidSandnodeLinkException, CannotUseEncryption {
        EncryptionManager.registerAll();

        AsymmetricKeyStorage eciesKeyStorage = AsymmetricEncryptions.ECIES.generate();

        String validLink = "sandnode://hub@localhost:52525?encryption=ECIES&key=%s"
                .formatted(eciesKeyStorage.encodedPublicKey());

        SandnodeLinkRecord record = Links.parse(validLink);

        assertNotNull(record);
        assertEquals("localhost", record.endpoint().host());
        assertEquals(52525, record.endpoint().port());
        assertNotNull(record.keyStorage());
    }

    @Test
    public void testParseInvalidScheme() {
        String invalidLink = "http://test@localhost:52525?encryption=ECIES&key=testPublicKey";

        assertThrows(InvalidSandnodeLinkException.class, () -> Links.parse(invalidLink));
    }

    @Test
    public void testParseMissingEncryption() {
        String invalidLink = "sandnode://test@localhost:52525?key=testPublicKey";

        assertThrows(InvalidSandnodeLinkException.class, () -> Links.parse(invalidLink));
    }

    @Test
    public void testParseMissingKey() {
        String invalidLink = "sandnode://test@localhost:52525?encryption=ECIES";

        assertThrows(InvalidSandnodeLinkException.class, () -> Links.parse(invalidLink));
    }

    private SandnodeServer createTestServer() {
        return new SandnodeServer(
            new TestHub(),
            new TestServerConfig()
        );
    }

    static class TestServerConfig implements ServerConfig {
        public Endpoint endpoint() {
            return new Endpoint("localhost", 52525);
        }

        @Override
        public Path publicKeyPath() {
            return null;
        }

        @Override
        public Path privateKeyPath() {
            return null;
        }

        @Override
        public @NotNull AsymmetricEncryption mainEncryption() {
            return AsymmetricEncryptions.ECIES;
        }

        @Override
        public GroupManager groupManager() {
            return null;
        }

        @Override
        public Database database() {
            return null;
        }

        @Override
        public Tokenizer tokenizer() {
            return null;
        }
    }

    private static class TestHub extends Hub {
        public TestHub() {
            super(new KeyStorageRegistry(AsymmetricEncryptions.ECIES.generate()));
        }

        @Override
        protected ServerChainManager createChainManager() {
            return null;
        }
    }
}
