package net.result.sandnode.link;

import net.result.sandnode.encryption.interfaces.AsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.exception.crypto.CannotUseEncryption;
import net.result.sandnode.exception.crypto.EncryptionTypeException;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.message.util.NodeType;
import net.result.sandnode.serverclient.SandnodeServer;
import net.result.sandnode.util.Address;
import net.result.sandnode.util.NetworkUtil;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public record SandnodeLinkRecord(NodeType nodeType, Address address, AsymmetricKeyStorage keyStorage) {
    public static SandnodeLinkRecord fromServer(SandnodeServer server)
            throws EncryptionTypeException, KeyStorageNotFoundException {
        NodeType type = server.node.type();
        Address address = server.serverConfig.address();
        AsymmetricEncryption encryption = server.serverConfig.mainEncryption();
        AsymmetricKeyStorage keyStorage = server.node.keyStorageRegistry.asymmetricNonNull(encryption);
        return new SandnodeLinkRecord(type, address, keyStorage);
    }

    public URI getURI() {
        return URI.create(toString());
    }

    @Override
    public @NotNull String toString() {
        try {
            return "sandnode://%s@%s?encryption=%s&key=%s".formatted(
                    URLEncoder.encode(nodeType.name().toLowerCase(), StandardCharsets.UTF_8),
                    NetworkUtil.replaceZeroes(address, 52525),
                    URLEncoder.encode(keyStorage.encryption().name(), StandardCharsets.UTF_8),
                    URLEncoder.encode(keyStorage.encodedPublicKey(), StandardCharsets.UTF_8)
            );
        } catch (CannotUseEncryption e) {
            throw new RuntimeException(e);
        }
    }
}

