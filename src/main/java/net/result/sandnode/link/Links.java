package net.result.sandnode.link;

import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.interfaces.AsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.crypto.EncryptionTypeException;
import net.result.sandnode.exception.crypto.NoSuchEncryptionException;
import net.result.sandnode.message.util.NodeType;
import net.result.sandnode.util.Address;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

public class Links {
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
                        encryptionType = keyValue[1];
                    } else if (keyValue[0].equals("key")) {
                        encodedKey = keyValue[1];
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
