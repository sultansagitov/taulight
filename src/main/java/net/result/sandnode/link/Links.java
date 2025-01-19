package net.result.sandnode.link;

import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.IAsymmetricKeyStorage;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.server.SandnodeServer;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.util.NetworkUtil;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Links {
    public static @NotNull URI getServerLink(@NotNull SandnodeServer server)
            throws KeyStorageNotFoundException, EncryptionTypeException {
        IAsymmetricEncryption encryption = server.serverConfig.mainEncryption();
        IAsymmetricKeyStorage keyStorage = server.node.globalKeyStorage.getAsymmetricNonNull(encryption);
        String string;
        try {
            string = "sandnode://%s@%s?encryption=%s&key=%s".formatted(
                    URLEncoder.encode(server.node.type().name().toLowerCase(), UTF_8),
                    NetworkUtil.replaceZeroes(server.serverConfig.endpoint(), 52525),
                    URLEncoder.encode(encryption.name(), UTF_8),
                    URLEncoder.encode(keyStorage.encodedPublicKey(), UTF_8)
            );
        } catch (CannotUseEncryption e) {
            throw new ImpossibleRuntimeException(e);
        }
        return URI.create(string);
    }
    
    public static SandnodeLinkRecord parse(String s) throws InvalidSandnodeLinkException, CreatingKeyException {
        URI uri;
        try {
            uri = new URI(s);
        } catch (URISyntaxException e) {
            throw new InvalidSandnodeLinkException(e);
        }

        Endpoint endpoint = new Endpoint(uri.getHost(), uri.getPort() == -1 ? 52525 : uri.getPort());

        if (!"sandnode".equals(uri.getScheme())) {
            throw new InvalidSandnodeLinkException("Invalid scheme: " + uri.getScheme());
        }

        String nodeType = uri.getUserInfo();
        if (nodeType == null) {
            throw new InvalidSandnodeLinkException("User info cannot be null");
        }

        String encryptionType = null;
        String encodedKey = null;
        String query = uri.getQuery();
        if (query != null) {
            String[] queryParams = query.split("&");
            for (String param : queryParams) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    if ("encryption".equals(keyValue[0])) {
                        encryptionType = keyValue[1];
                    } else if ("key".equals(keyValue[0])) {
                        encodedKey = keyValue[1];
                    }
                }
            }
        }

        if (encryptionType == null) {
            throw new InvalidSandnodeLinkException("Encryption type not found in query parameters");
        }
        if (encodedKey == null) {
            throw new InvalidSandnodeLinkException("Key not found in query parameters");
        }

        IAsymmetricEncryption encryption;
        try {
            encryption = EncryptionManager.find(encryptionType).asymmetric();
        } catch (NoSuchEncryptionException | EncryptionTypeException e) {
            throw new InvalidSandnodeLinkException("Unknown encryption type: " + encryptionType, e);
        }

        IAsymmetricKeyStorage keyStorage = encryption.publicKeyConvertor().toKeyStorage(encodedKey);

        return new SandnodeLinkRecord(endpoint, keyStorage);
    }
}
