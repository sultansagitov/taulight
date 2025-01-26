package net.result.sandnode.encryption;

import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.encryption.interfaces.SymmetricKeyStorage;
import net.result.sandnode.exception.CannotUseEncryption;
import net.result.sandnode.exception.EncryptionTypeException;
import net.result.sandnode.exception.KeyStorageNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static net.result.sandnode.encryption.AsymmetricEncryptions.ECIES;
import static net.result.sandnode.encryption.AsymmetricEncryptions.RSA;
import static net.result.sandnode.encryption.SymmetricEncryptions.AES;
import static org.junit.jupiter.api.Assertions.*;

class GlobalKeyStorageTest {

    private GlobalKeyStorage globalKeyStorage;

    private KeyStorage rsaKeyStorage;
    private KeyStorage eciesKeyStorage;
    private KeyStorage aesKeyStorage;

    @BeforeEach
    void setUp() {
        globalKeyStorage = new GlobalKeyStorage();
        EncryptionManager.registerAll();

        rsaKeyStorage = RSA.generate();
        eciesKeyStorage = ECIES.generate();
        aesKeyStorage = AES.generate();
    }

    @Test
    void testSetAndGetNonNull() throws KeyStorageNotFoundException {
        globalKeyStorage.set(RSA, rsaKeyStorage);
        KeyStorage retrieved = globalKeyStorage.getNonNull(RSA);
        assertEquals(rsaKeyStorage, retrieved);
    }

    @Test
    void testSetAndGetOptional() {
        globalKeyStorage.set(ECIES, eciesKeyStorage);
        Optional<KeyStorage> retrieved = globalKeyStorage.get(ECIES);
        assertTrue(retrieved.isPresent());
        assertEquals(eciesKeyStorage, retrieved.get());
    }

    @Test
    void testHasEncryption() {
        globalKeyStorage.set(AES, aesKeyStorage);
        assertTrue(globalKeyStorage.has(AES));
    }

    @Test
    void testGetAsymmetricNonNull() throws KeyStorageNotFoundException, EncryptionTypeException {
        globalKeyStorage.set(RSA, rsaKeyStorage);
        AsymmetricKeyStorage retrieved = globalKeyStorage.getAsymmetricNonNull(RSA);
        assertNotNull(retrieved);
    }

    @Test
    void testGetAsymmetricNonNullThrowsExceptionWhenNotFound() {
        assertThrows(KeyStorageNotFoundException.class, () -> globalKeyStorage.getAsymmetricNonNull(ECIES));
    }

    @Test
    void testGetSymmetricNonNull() throws CannotUseEncryption, EncryptionTypeException {
        globalKeyStorage.set(AES, aesKeyStorage);
        SymmetricKeyStorage retrieved = globalKeyStorage.getSymmetricNonNull(AES);
        assertNotNull(retrieved);
    }

    @Test
    void testCopy() throws Exception {
        globalKeyStorage.set(RSA, rsaKeyStorage);
        GlobalKeyStorage copy = globalKeyStorage.copy();
        KeyStorage copiedKeyStorage = copy.getNonNull(RSA);

        String originalData = "HelloWorld";

        byte[] originalEncrypted = RSA.encrypt(originalData, rsaKeyStorage);
        byte[] copyEncrypted = RSA.encrypt(originalData, copiedKeyStorage);

        String originalDecrypted = RSA.decrypt(originalEncrypted, copiedKeyStorage);
        String copyDecrypted = RSA.decrypt(copyEncrypted, rsaKeyStorage);

        assertEquals(originalDecrypted, copyDecrypted);
    }

    @Test
    void testToString() {
        globalKeyStorage.set(RSA, rsaKeyStorage);
        String result = globalKeyStorage.toString();
        assertTrue(result.contains("RSA"));
        assertTrue(result.contains("RSAKeyStorage"));
    }
}
