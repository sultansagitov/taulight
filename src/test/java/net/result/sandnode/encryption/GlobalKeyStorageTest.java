package net.result.sandnode.encryption;

import net.result.sandnode.encryption.interfaces.*;
import net.result.sandnode.exceptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static net.result.sandnode.encryption.AsymmetricEncryption.ECIES;
import static net.result.sandnode.encryption.AsymmetricEncryption.RSA;
import static net.result.sandnode.encryption.SymmetricEncryption.AES;
import static org.junit.jupiter.api.Assertions.*;

class GlobalKeyStorageTest {

    private GlobalKeyStorage globalKeyStorage;

    private IKeyStorage rsaKeyStorage;
    private IKeyStorage eciesKeyStorage;
    private IKeyStorage aesKeyStorage;

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
        IKeyStorage retrieved = globalKeyStorage.getNonNull(RSA);
        assertEquals(rsaKeyStorage, retrieved);
    }

    @Test
    void testSetAndGetOptional() {
        globalKeyStorage.set(ECIES, eciesKeyStorage);
        Optional<IKeyStorage> retrieved = globalKeyStorage.get(ECIES);
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
        IAsymmetricKeyStorage retrieved = globalKeyStorage.getAsymmetricNonNull(RSA);
        assertNotNull(retrieved);
    }

    @Test
    void testGetAsymmetricNonNullThrowsExceptionWhenNotFound() {
        assertThrows(KeyStorageNotFoundException.class, () -> globalKeyStorage.getAsymmetricNonNull(ECIES));
    }

    @Test
    void testGetSymmetricNonNull() throws CannotUseEncryption, EncryptionTypeException {
        globalKeyStorage.set(AES, aesKeyStorage);
        ISymmetricKeyStorage retrieved = globalKeyStorage.getSymmetricNonNull(AES);
        assertNotNull(retrieved);
    }

    @Test
    void testCopy() throws Exception {
        globalKeyStorage.set(RSA, rsaKeyStorage);
        GlobalKeyStorage copy = globalKeyStorage.copy();
        IKeyStorage copiedKeyStorage = copy.getNonNull(RSA);

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
