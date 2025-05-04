package net.result.sandnode.encryption;

import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.encryption.interfaces.SymmetricKeyStorage;
import net.result.sandnode.exception.crypto.CannotUseEncryption;
import net.result.sandnode.exception.crypto.EncryptionTypeException;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class KeyStorageRegistryTest {

    static KeyStorageRegistry keyStorageRegistry;

    static KeyStorage eciesKeyStorage;
    static KeyStorage aesKeyStorage;

    @BeforeAll
    static void setUp() {
        keyStorageRegistry = new KeyStorageRegistry();
        EncryptionManager.registerAll();

        eciesKeyStorage = AsymmetricEncryptions.ECIES.generate();
        aesKeyStorage = SymmetricEncryptions.AES.generate();
    }

    @Test
    void testSetAndGetNonNull() throws KeyStorageNotFoundException {
        keyStorageRegistry.set(AsymmetricEncryptions.ECIES, eciesKeyStorage);
        KeyStorage retrieved = keyStorageRegistry.getNonNull(AsymmetricEncryptions.ECIES);
        assertEquals(eciesKeyStorage, retrieved);
    }

    @Test
    void testSetAndGetOptional() {
        keyStorageRegistry.set(AsymmetricEncryptions.ECIES, eciesKeyStorage);
        Optional<KeyStorage> retrieved = keyStorageRegistry.get(AsymmetricEncryptions.ECIES);
        assertTrue(retrieved.isPresent());
        assertEquals(eciesKeyStorage, retrieved.get());
    }

    @Test
    void testHasEncryption() {
        keyStorageRegistry.set(SymmetricEncryptions.AES, aesKeyStorage);
        assertTrue(keyStorageRegistry.has(SymmetricEncryptions.AES));
    }

    @Test
    void testAsymmetricNonNull() throws KeyStorageNotFoundException, EncryptionTypeException {
        keyStorageRegistry.set(AsymmetricEncryptions.ECIES, eciesKeyStorage);
        AsymmetricKeyStorage retrieved = keyStorageRegistry.asymmetricNonNull(AsymmetricEncryptions.ECIES);
        assertNotNull(retrieved);
    }

    @Test
    void testGetForECIESReturnsEmptyOptional() {
        assertTrue(keyStorageRegistry.get(AsymmetricEncryptions.ECIES).isEmpty());
    }

    @Test
    void testSymmetricNonNull() throws CannotUseEncryption, EncryptionTypeException {
        keyStorageRegistry.set(SymmetricEncryptions.AES, aesKeyStorage);
        SymmetricKeyStorage retrieved = keyStorageRegistry.symmetricNonNull(SymmetricEncryptions.AES);
        assertNotNull(retrieved);
    }

    @Test
    void testCopy() throws Exception {
        keyStorageRegistry.set(AsymmetricEncryptions.ECIES, eciesKeyStorage);
        KeyStorageRegistry copy = keyStorageRegistry.copy();
        KeyStorage copiedKeyStorage = copy.getNonNull(AsymmetricEncryptions.ECIES);

        String originalData = "HelloWorld";

        byte[] originalEncrypted = AsymmetricEncryptions.ECIES.encrypt(originalData, eciesKeyStorage);
        byte[] copyEncrypted = AsymmetricEncryptions.ECIES.encrypt(originalData, copiedKeyStorage);

        String originalDecrypted = AsymmetricEncryptions.ECIES.decrypt(originalEncrypted, copiedKeyStorage);
        String copyDecrypted = AsymmetricEncryptions.ECIES.decrypt(copyEncrypted, eciesKeyStorage);

        assertEquals(originalDecrypted, copyDecrypted);
    }

    @Test
    void testToString() {
        keyStorageRegistry.set(AsymmetricEncryptions.ECIES, eciesKeyStorage);
        String result = keyStorageRegistry.toString();
        assertTrue(result.contains("ECIES"));
        assertTrue(result.contains("ECIESKeyStorage"));
    }
}
