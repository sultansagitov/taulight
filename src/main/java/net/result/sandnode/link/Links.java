package net.result.sandnode.link;

import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.interfaces.AsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.crypto.CreatingKeyException;
import net.result.sandnode.exception.crypto.EncryptionTypeException;
import net.result.sandnode.exception.crypto.NoSuchEncryptionException;
import net.result.sandnode.message.util.NodeType;
import net.result.sandnode.util.Endpoint;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

public class Links {

    public static SandnodeLinkRecord parse(String s) throws InvalidSandnodeLinkException, CreatingKeyException {
        URI uri;
        try {
            uri = new URI(s);
        } catch (URISyntaxException e) {
            throw new InvalidSandnodeLinkException(e);
        }

        Endpoint endpoint = new Endpoint(uri.getHost(), uri.getPort() == -1 ? 52525 : uri.getPort());

        if (!Objects.equals(uri.getScheme(), "sandnode")) {
            throw new InvalidSandnodeLinkException("Invalid scheme: " + uri.getScheme());
        }

        String nodeType = uri.getUserInfo();
        NodeType type;

        if (nodeType == null) throw new InvalidSandnodeLinkException("Node type cannot be null");

        try {
            type = Enum.valueOf(NodeType.class, nodeType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidSandnodeLinkException("Incorrect node type (hub or agent) - nodeType");
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
                        encryptionType = keyValue[1];
                    } else if (keyValue[0].equals("key")) {
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

        AsymmetricEncryption encryption;
        try {
            encryption = EncryptionManager.find(encryptionType).asymmetric();
        } catch (NoSuchEncryptionException | EncryptionTypeException e) {
            throw new InvalidSandnodeLinkException("Unknown encryption type: " + encryptionType, e);
        }

        AsymmetricKeyStorage keyStorage = encryption.publicKeyConvertor().toKeyStorage(encodedKey);

        return new SandnodeLinkRecord(type, endpoint, keyStorage);
    }
}
