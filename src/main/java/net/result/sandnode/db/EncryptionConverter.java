package net.result.sandnode.db;

import jakarta.persistence.AttributeConverter;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.interfaces.Encryption;
import net.result.sandnode.exception.crypto.NoSuchEncryptionException;

public class EncryptionConverter implements AttributeConverter<Encryption, String> {

    @Override
    public String convertToDatabaseColumn(Encryption encryption) {
        return encryption.name();
    }

    @Override
    public Encryption convertToEntityAttribute(String dbData) {
        try {
            return EncryptionManager.find(dbData);
        } catch (NoSuchEncryptionException e) {
            throw new RuntimeException(e);
        }
    }
}