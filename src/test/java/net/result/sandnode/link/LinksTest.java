package net.result.sandnode.link;

import net.result.sandnode.config.IServerConfig;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.GlobalKeyStorage;
import net.result.sandnode.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.IAsymmetricKeyStorage;
import net.result.sandnode.exception.*;
import net.result.sandnode.serverclient.SandnodeServer;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.db.IDatabase;
import net.result.sandnode.group.GroupManager;
import net.result.sandnode.tokens.ITokenizer;
import net.result.taulight.TauHub;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;

import static net.result.sandnode.encryption.AsymmetricEncryption.RSA;
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

        IAsymmetricKeyStorage rsaKeyStorage = RSA.generate();

        String validLink = "sandnode://hub@localhost:52525?encryption=RSA&key=" + rsaKeyStorage.encodedPublicKey();

        SandnodeLinkRecord record = Links.parse(validLink);

        assertNotNull(record);
        assertEquals("localhost", record.endpoint().host());
        assertEquals(52525, record.endpoint().port());
        assertNotNull(record.keyStorage());
    }

    @Test
    public void testParseInvalidScheme() {
        String invalidLink = "http://test@localhost:52525?encryption=RSA&key=testPublicKey";

        assertThrows(InvalidSandnodeLinkException.class, () -> Links.parse(invalidLink));
    }

    @Test
    public void testParseMissingEncryption() {
        String invalidLink = "sandnode://test@localhost:52525?key=testPublicKey";

        assertThrows(InvalidSandnodeLinkException.class, () -> Links.parse(invalidLink));
    }

    @Test
    public void testParseMissingKey() {
        String invalidLink = "sandnode://test@localhost:52525?encryption=RSA";

        assertThrows(InvalidSandnodeLinkException.class, () -> Links.parse(invalidLink));
    }

    private SandnodeServer createTestServer() {
        return new SandnodeServer(
            new TauHub(new GlobalKeyStorage(RSA.generate()), null),
            new TestServerConfig()
        );
    }

    static class TestServerConfig implements IServerConfig {
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
        public @NotNull IAsymmetricEncryption mainEncryption() {
            return RSA;
        }

        @Override
        public GroupManager groupManager() {
            return null;
        }

        @Override
        public IDatabase database() {
            return null;
        }

        @Override
        public ITokenizer tokenizer() {
            return null;
        }
    }
}
