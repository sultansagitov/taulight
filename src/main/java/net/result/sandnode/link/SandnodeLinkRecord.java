package net.result.sandnode.link;

import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.interfaces.AsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.exception.InvalidSandnodeLinkException;
import net.result.sandnode.exception.crypto.CannotUseEncryption;
import net.result.sandnode.exception.crypto.EncryptionTypeException;
import net.result.sandnode.exception.crypto.NoSuchEncryptionException;
import net.result.sandnode.message.util.NodeType;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.serverclient.SandnodeServer;
import net.result.sandnode.util.Address;
import net.result.sandnode.util.NetworkUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public record SandnodeLinkRecord(NodeType nodeType, Address address, @Nullable AsymmetricKeyStorage keyStorage) {
    public URI getURI() {
        try {
            String scheme = "sandnode";
            String userInfo = URLEncoder.encode(nodeType.name().toLowerCase(), StandardCharsets.UTF_8);
            String host = NetworkUtil.replaceZeroes(address, 52525);

            String query = null;
            if (keyStorage != null) {
                query = "encryption=%s&key=%s".formatted(
                        URLEncoder.encode(keyStorage.encryption().name(), StandardCharsets.UTF_8),
                        URLEncoder.encode(keyStorage.encodedPublicKey(), StandardCharsets.UTF_8)
                );
            }

            return new URI(scheme, userInfo, host, -1, null, query, null);
        } catch (CannotUseEncryption e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public @NotNull String toString() {
        return getURI().toString();
    }


    public static SandnodeLinkRecord fromServer(SandnodeServer server) {
        NodeType type = server.node.type();
        Address address = server.serverConfig.address();
        AsymmetricEncryption encryption = server.serverConfig.mainEncryption();
        AsymmetricKeyStorage keyStorage = server.node.keyStorageRegistry.asymmetricNonNull(encryption);
        return new SandnodeLinkRecord(type, address, keyStorage);
    }

    public static SandnodeLinkRecord fromClient(SandnodeClient client) {
        NodeType type = client.nodeType;
        Address address = client.address;
        AsymmetricKeyStorage keyStorage = client.node().agent().config.loadServerKey(client.address);
        return new SandnodeLinkRecord(type, address, keyStorage);
    }

    public static SandnodeLinkRecord parse(String s, NodeType defaultNodeType) {
        URI uri;
        try {
            uri = new URI(s);
        } catch (URISyntaxException e) {
            try {
                uri = new URI("sandnode://" + s);
            } catch (URISyntaxException ex) {
                throw new InvalidSandnodeLinkException(e);
            }
        }

        if (uri.getHost() == null) {
            try {
                uri = new URI("sandnode://" + s);
            } catch (URISyntaxException e) {
                throw new InvalidSandnodeLinkException(e);
            }
        }

        String host = uri.getHost();
        if (host == null) {
            throw new InvalidSandnodeLinkException("Host should be not null");
        }
        Address address = new Address(host, uri.getPort() == -1 ? 52525 : uri.getPort());

        String scheme = uri.getScheme();
        if (scheme != null && !Objects.equals(scheme, "sandnode")) {
            throw new InvalidSandnodeLinkException("Invalid scheme: " + scheme);
        }

        NodeType type = defaultNodeType;
        String nodeType = uri.getUserInfo();
        if (nodeType != null) {
            try {
                type = Enum.valueOf(NodeType.class, nodeType.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new InvalidSandnodeLinkException("Incorrect node type (hub or agent) - nodeType");
            }
        }
        String encryptionType = null;
        String encodedKey = null;
        String query = uri.getQuery();
        if (query != null) {
            String[] queryParams = query.split("&");
            for (String param : queryParams) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    if (keyValue[0].equals("encryption")) {
                        encryptionType = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                    } else if (keyValue[0].equals("key")) {
                        encodedKey = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                    }
                }
            }
        }

        AsymmetricKeyStorage keyStorage = null;

        if (encryptionType != null && encodedKey != null) {
            try {
                AsymmetricEncryption encryption = EncryptionManager.find(encryptionType).asymmetric();
                keyStorage = encryption.publicKeyConvertor().toKeyStorage(encodedKey);
            } catch (NoSuchEncryptionException | EncryptionTypeException e) {
                throw new InvalidSandnodeLinkException("Unknown encryption type: " + encryptionType, e);
            }
        }

        return new SandnodeLinkRecord(type, address, keyStorage);
    }
}
