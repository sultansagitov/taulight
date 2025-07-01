package net.result.sandnode.link;

import net.result.sandnode.chain.ServerChainManager;
import net.result.sandnode.config.HubConfigRecord;
import net.result.sandnode.config.ServerConfig;
import net.result.sandnode.encryption.AsymmetricEncryptions;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.KeyStorageRegistry;
import net.result.sandnode.encryption.interfaces.AsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.exception.InvalidSandnodeLinkException;
import net.result.sandnode.exception.crypto.CannotUseEncryption;
import net.result.sandnode.exception.crypto.CreatingKeyException;
import net.result.sandnode.exception.crypto.EncryptionTypeException;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.hubagent.Hub;
import net.result.sandnode.serverclient.SandnodeServer;
import net.result.sandnode.util.Address;
import net.result.sandnode.util.Container;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

public class LinksTest {
    private static KeyStorageRegistry hubKeyStorage;
    private static HubConfigRecord config;

    @BeforeAll
    static void setup() {
        EncryptionManager.registerAll();
        AsymmetricKeyStorage generated = AsymmetricEncryptions.ECIES.generate();
        hubKeyStorage = new KeyStorageRegistry(generated);
        config = new HubConfigRecord("Test Hub", null, null);
    }

    @Test
    public void testGetServerLink() throws KeyStorageNotFoundException, EncryptionTypeException {
        SandnodeServer server = new SandnodeServer(new TestHub(), new TestServerConfig());

        URI serverLink = SandnodeLinkRecord.fromServer(server).getURI();

        assertNotNull(serverLink);
        assertEquals("sandnode", serverLink.getScheme());
        assertTrue(serverLink.getHost().contains("localhost"));
        assertTrue(serverLink.getQuery().contains("encryption=ECIES"));
        assertTrue(serverLink.getQuery().contains("key="));
    }

    @Test
    public void testParse() throws CreatingKeyException, InvalidSandnodeLinkException, CannotUseEncryption {
        AsymmetricKeyStorage eciesKeyStorage = AsymmetricEncryptions.ECIES.generate();

        String validLink = "sandnode://hub@localhost:52525?encryption=ECIES&key=%s"
                .formatted(eciesKeyStorage.encodedPublicKey());

        SandnodeLinkRecord record = Links.parse(validLink);

        assertNotNull(record);
        assertEquals("localhost", record.address().host());
        assertEquals(52525, record.address().port());
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

    @Test
    public void testParseInvalidPublicKey() {
        String invalidLink = "sandnode://test@localhost:52525?encryption=ECIES&key=INVALID_BASE64";

        assertThrows(InvalidSandnodeLinkException.class, () -> Links.parse(invalidLink));
    }

    static class TestServerConfig implements ServerConfig {
        @Override
        public Container container() {
            return null;
        }

        @Override
        public Address address() {
            return new Address("localhost", 52525);
        }

        @Override
        public @NotNull AsymmetricEncryption mainEncryption() {
            return AsymmetricEncryptions.ECIES;
        }

        @Override
        public void saveKey(AsymmetricKeyStorage keyStorage) {}

        @Override
        public KeyStorageRegistry readKey(AsymmetricEncryption mainEncryption) {
            return null;
        }
    }

    private static class TestHub extends Hub {
        public TestHub() {
            super(hubKeyStorage, LinksTest.config);
        }

        @SuppressWarnings("DataFlowIssue")
        @Override
        protected @NotNull ServerChainManager createChainManager() {
            return null;
        }
    }
}
