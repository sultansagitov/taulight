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
import net.result.sandnode.hubagent.Hub;
import net.result.sandnode.message.util.NodeType;
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
    public void testGetServerLink() {
        SandnodeServer server = new SandnodeServer(new TestHub(), new TestServerConfig());

        SandnodeLinkRecord record = SandnodeLinkRecord.fromServer(server);
        URI serverLink = record.getURI();

        assertNotNull(serverLink);
        assertEquals("sandnode", serverLink.getScheme());
        assertTrue(serverLink.getHost().contains("localhost"));
        assertTrue(serverLink.getQuery().contains("encryption=ECIES"));
        assertTrue(serverLink.getQuery().contains("key="));

        assertNotNull(record);
        assertNotNull(record.address());
        assertEquals("localhost", record.address().host());
        assertEquals(52525, record.address().port());
        assertNotNull(record.keyStorage());

        assertEquals("hub", serverLink.getUserInfo());
    }

    @Test
    public void testParse() {
        AsymmetricKeyStorage eciesKeyStorage = AsymmetricEncryptions.ECIES.generate();

        String validLink = "sandnode://hub@localhost:52525?encryption=ECIES&key=%s"
                .formatted(eciesKeyStorage.encodedPublicKey());

        SandnodeLinkRecord record = SandnodeLinkRecord.parse(validLink, NodeType.HUB);

        assertNotNull(record);
        assertEquals(NodeType.HUB, record.nodeType(), "nodeType should reflect the userinfo or default");
        assertEquals("localhost", record.address().host());
        assertEquals(52525, record.address().port());
        assertNotNull(record.keyStorage());

        assertNotNull(record.getURI());
        assertEquals("hub", record.getURI().getUserInfo());
        assertTrue(record.getURI().getQuery().contains("encryption=ECIES"));
        assertTrue(record.getURI().getQuery().contains("key="));
    }

    @Test
    public void testParseExplicitHubUserInfoOverridesDefault() {
        AsymmetricKeyStorage eciesKey = AsymmetricEncryptions.ECIES.generate();
        String link = "sandnode://hub@localhost:52525?encryption=ECIES&key=%s".formatted(eciesKey.encodedPublicKey());

        SandnodeLinkRecord record = SandnodeLinkRecord.parse(link, NodeType.AGENT);

        assertNotNull(record);
        assertEquals(NodeType.HUB, record.nodeType());
        assertEquals("hub", record.getURI().getUserInfo());
        assertEquals("localhost", record.address().host());
    }

    @Test
    public void testParseExplicitAgentUserInfoOverridesDefault() {
        AsymmetricKeyStorage eciesKey = AsymmetricEncryptions.ECIES.generate();
        String link = "sandnode://agent@127.0.0.1:52525?encryption=ECIES&key=%s".formatted(eciesKey.encodedPublicKey());

        SandnodeLinkRecord record = SandnodeLinkRecord.parse(link, NodeType.HUB);

        assertNotNull(record);
        assertEquals(NodeType.AGENT, record.nodeType());
        assertEquals("agent", record.getURI().getUserInfo());
        assertEquals("127.0.0.1", record.address().host());
    }

    @Test
    public void testDefaultNodeTypeAppliedWhenNoUserInfo() {
        SandnodeLinkRecord record = SandnodeLinkRecord.parse("localhost", NodeType.AGENT);
        assertNotNull(record);
        assertEquals(NodeType.AGENT, record.nodeType(), "When no userinfo present, default node type must be applied");
        assertEquals("localhost", record.address().host());
        assertEquals(52525, record.address().port());
        assertEquals("agent", record.getURI().getUserInfo());
        assertNull(record.keyStorage());
    }

    @Test
    public void testParseInvalidScheme() {
        String invalidLink = "http://test@localhost:52525?encryption=ECIES&key=testPublicKey";
        assertThrows(InvalidSandnodeLinkException.class, () -> SandnodeLinkRecord.parse(invalidLink, NodeType.HUB));
    }

    @Test
    public void testParseMissingEncryption() {
        String invalidLink = "sandnode://test@localhost:52525?key=testPublicKey";
        assertThrows(InvalidSandnodeLinkException.class, () -> SandnodeLinkRecord.parse(invalidLink, NodeType.HUB));
    }

    @Test
    public void testParseMissingKey() {
        String invalidLink = "sandnode://test@localhost:52525?encryption=ECIES";
        assertThrows(InvalidSandnodeLinkException.class, () -> SandnodeLinkRecord.parse(invalidLink, NodeType.HUB));
    }

    @Test
    public void testParseInvalidPublicKey() {
        String invalidLink = "sandnode://test@localhost:52525?encryption=ECIES&key=INVALID_BASE64";
        assertThrows(InvalidSandnodeLinkException.class, () -> SandnodeLinkRecord.parse(invalidLink, NodeType.HUB));
    }

    @Test
    public void testParseBareLocalhost() {
        SandnodeLinkRecord record = SandnodeLinkRecord.parse("localhost", NodeType.HUB);
        assertNotNull(record);
        assertEquals(NodeType.HUB, record.nodeType());
        assertEquals("localhost", record.address().host());
        assertEquals(52525, record.address().port());
        assertNull(record.keyStorage());
        assertEquals("hub", record.getURI().getUserInfo());
    }

    @Test
    public void testParseBareIPv4() {
        SandnodeLinkRecord record = SandnodeLinkRecord.parse("127.0.0.1", NodeType.HUB);
        assertNotNull(record);
        assertEquals(NodeType.HUB, record.nodeType());
        assertEquals("127.0.0.1", record.address().host());
        assertEquals(52525, record.address().port());
        assertNull(record.keyStorage());
    }

    @Test
    public void testParseBareIPv6() {
        SandnodeLinkRecord record = SandnodeLinkRecord.parse("[::1]", NodeType.HUB);
        assertNotNull(record);
        assertEquals(NodeType.HUB, record.nodeType());
        assertEquals("[::1]", record.address().host());
        assertEquals(52525, record.address().port());
        assertNull(record.keyStorage());
    }

    @Test
    public void testParseBareDomain() {
        SandnodeLinkRecord record = SandnodeLinkRecord.parse("example.com", NodeType.HUB);
        assertNotNull(record);
        assertEquals(NodeType.HUB, record.nodeType());
        assertEquals("example.com", record.address().host());
        assertEquals(52525, record.address().port());
        assertNull(record.keyStorage());
    }

    @Test
    public void testParseBareLocalhostWithPort() {
        SandnodeLinkRecord record = SandnodeLinkRecord.parse("localhost:60000", NodeType.HUB);
        assertNotNull(record);
        assertEquals(NodeType.HUB, record.nodeType());
        assertEquals("localhost", record.address().host());
        assertEquals(60000, record.address().port());
        assertNull(record.keyStorage());
    }

    @Test
    public void testParseBareIPv4WithPort() {
        SandnodeLinkRecord record = SandnodeLinkRecord.parse("127.0.0.1:12345", NodeType.HUB);
        assertNotNull(record);
        assertEquals(NodeType.HUB, record.nodeType());
        assertEquals("127.0.0.1", record.address().host());
        assertEquals(12345, record.address().port());
        assertNull(record.keyStorage());
    }

    @Test
    public void testParseBareIPv6WithPort() {
        SandnodeLinkRecord record = SandnodeLinkRecord.parse("[::1]:9999", NodeType.HUB);
        assertNotNull(record);
        assertEquals(NodeType.HUB, record.nodeType());
        assertEquals("[::1]", record.address().host());
        assertEquals(9999, record.address().port());
        assertNull(record.keyStorage());
    }

    @Test
    public void testParseBareDomainWithPort() {
        SandnodeLinkRecord record = SandnodeLinkRecord.parse("example.com:443", NodeType.HUB);
        assertNotNull(record);
        assertEquals(NodeType.HUB, record.nodeType());
        assertEquals("example.com", record.address().host());
        assertEquals(443, record.address().port());
        assertNull(record.keyStorage());
    }

    @Test
    public void testParseInvalidBareHost() {
        assertThrows(InvalidSandnodeLinkException.class, () -> SandnodeLinkRecord.parse(":1234", NodeType.HUB));
        assertThrows(InvalidSandnodeLinkException.class, () -> SandnodeLinkRecord.parse("[]", NodeType.HUB));
    }

    @Test
    public void testParseHostWithSpaces() {
        assertThrows(InvalidSandnodeLinkException.class, () -> SandnodeLinkRecord.parse("exa mple.com", NodeType.HUB));
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
        public @NotNull ServerChainManager createChainManager() {
            return null;
        }
    }
}
