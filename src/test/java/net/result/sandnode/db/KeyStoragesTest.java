package net.result.sandnode.db;

import net.result.sandnode.GlobalTestState;
import net.result.sandnode.encryption.AsymmetricEncryptions;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.crypto.CannotUseEncryption;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.JPAUtil;
import net.result.sandnode.util.SimpleJPAUtil;
import org.junit.jupiter.api.*;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class KeyStoragesTest {

    private static KeyStorageRepository keyStorageRepo;
    private static JPAUtil jpaUtil;

    @BeforeAll
    static void setup() {
        EncryptionManager.registerAll();
        Container container = GlobalTestState.container;
        keyStorageRepo = container.get(KeyStorageRepository.class);
        jpaUtil = container.get(SimpleJPAUtil.class);
    }

    @Test
    void testCreateAndFindKeyStorageEntity() throws CannotUseEncryption, DatabaseException {
        AsymmetricKeyStorage generatedKey = AsymmetricEncryptions.ECIES.generate();

        KeyStorageEntity saved = keyStorageRepo.create(generatedKey);
        assertNotNull(saved);
        assertNotNull(saved.id());
        assertEquals(generatedKey.encryption(), saved.encryption());
        assertEquals(generatedKey.encodedPublicKey(), saved.encodedKey);

        Optional<KeyStorageEntity> found = jpaUtil.find(KeyStorageEntity.class, saved.id());
        assertTrue(found.isPresent());
        assertEquals(saved, found.get());
    }

    @Test
    void testFindNonExistentKeyStorageEntity() throws DatabaseException {
        UUID randomId = UUID.randomUUID();
        Optional<KeyStorageEntity> result = jpaUtil.find(KeyStorageEntity.class, randomId);
        assertTrue(result.isEmpty());
    }

    @Test
    void testUniqueIdGenerationAvoidsCollision() throws CannotUseEncryption, DatabaseException {
        AsymmetricKeyStorage key1 = AsymmetricEncryptions.ECIES.generate();
        AsymmetricKeyStorage key2 = AsymmetricEncryptions.ECIES.generate();

        KeyStorageEntity e1 = keyStorageRepo.create(key1);
        KeyStorageEntity e2 = keyStorageRepo.create(key2);

        assertNotEquals(e1, e2);
    }
}
