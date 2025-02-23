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

import static org.junit.jupiter.api.Assertions.*;

class GlobalKeyStorageTest {

    private GlobalKeyStorage globalKeyStorage;

    private KeyStorage eciesKeyStorage;
    private KeyStorage aesKeyStorage;

    @BeforeEach
    void setUp() {
        globalKeyStorage = new GlobalKeyStorage();
        EncryptionManager.registerAll();

        eciesKeyStorage = AsymmetricEncryptions.ECIES.generate();
        aesKeyStorage = SymmetricEncryptions.AES.generate();
    }

    @Test
    void testSetAndGetNonNull() throws KeyStorageNotFoundException {
        globalKeyStorage.set(AsymmetricEncryptions.ECIES, eciesKeyStorage);
        KeyStorage retrieved = globalKeyStorage.getNonNull(AsymmetricEncryptions.ECIES);
        assertEquals(eciesKeyStorage, retrieved);
    }

    @Test
    void testSetAndGetOptional() {
        globalKeyStorage.set(AsymmetricEncryptions.ECIES, eciesKeyStorage);
        Optional<KeyStorage> retrieved = globalKeyStorage.get(AsymmetricEncryptions.ECIES);
        assertTrue(retrieved.isPresent());
        assertEquals(eciesKeyStorage, retrieved.get());
    }

    @Test
    void testHasEncryption() {
        globalKeyStorage.set(SymmetricEncryptions.AES, aesKeyStorage);
        assertTrue(globalKeyStorage.has(SymmetricEncryptions.AES));
    }

    @Test
    void testAsymmetricNonNull() throws KeyStorageNotFoundException, EncryptionTypeException {
        globalKeyStorage.set(AsymmetricEncryptions.ECIES, eciesKeyStorage);
        AsymmetricKeyStorage retrieved = globalKeyStorage.asymmetricNonNull(AsymmetricEncryptions.ECIES);
        assertNotNull(retrieved);
    }

    @Test
    void testGetForECIESReturnsEmptyOptional() {
        assertTrue(globalKeyStorage.get(AsymmetricEncryptions.ECIES).isEmpty());
    }

    @Test
    void testSymmetricNonNull() throws CannotUseEncryption, EncryptionTypeException {
        globalKeyStorage.set(SymmetricEncryptions.AES, aesKeyStorage);
        SymmetricKeyStorage retrieved = globalKeyStorage.symmetricNonNull(SymmetricEncryptions.AES);
        assertNotNull(retrieved);
    }

    @Test
    void testCopy() throws Exception {
        globalKeyStorage.set(AsymmetricEncryptions.ECIES, eciesKeyStorage);
        GlobalKeyStorage copy = globalKeyStorage.copy();
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
        globalKeyStorage.set(AsymmetricEncryptions.ECIES, eciesKeyStorage);
        String result = globalKeyStorage.toString();
        assertTrue(result.contains("ECIES"));
        assertTrue(result.contains("ECIESKeyStorage"));
    }
}
