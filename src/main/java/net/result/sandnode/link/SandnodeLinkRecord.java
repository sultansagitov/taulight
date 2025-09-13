package net.result.sandnode.link;

import net.result.sandnode.encryption.interfaces.AsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.exception.crypto.CannotUseEncryption;
import net.result.sandnode.message.util.NodeType;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.serverclient.SandnodeServer;
import net.result.sandnode.util.Address;
import net.result.sandnode.util.NetworkUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public record SandnodeLinkRecord(NodeType nodeType, Address address, @Nullable AsymmetricKeyStorage keyStorage) {
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

    public URI getURI() {
        return URI.create(toString());
    }

    @Override
    public @NotNull String toString() {
        try {
            String start = "sandnode://%s@%s".formatted(
                    URLEncoder.encode(nodeType.name().toLowerCase(), StandardCharsets.UTF_8),
                    NetworkUtil.replaceZeroes(address, 52525)
            );

            String end = keyStorage != null ? "?encryption=%s&key=%s".formatted(
                    URLEncoder.encode(keyStorage.encryption().name(), StandardCharsets.UTF_8),
                    URLEncoder.encode(keyStorage.encodedPublicKey(), StandardCharsets.UTF_8)
            ) : "";
            return start + end;
        } catch (CannotUseEncryption e) {
            throw new RuntimeException(e);
        }
    }
}

