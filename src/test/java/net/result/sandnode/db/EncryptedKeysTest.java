package net.result.sandnode.db;

import net.result.sandnode.GlobalTestState;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.BusyNicknameException;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.JPAUtil;
import net.result.sandnode.util.SimpleJPAUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class EncryptedKeysTest {
    private static EncryptedKeyRepository encryptedKeyRepo;
    private static MemberEntity sender;
    private static MemberEntity receiver;
    private static JPAUtil jpaUtil;

    @BeforeAll
    public static void setUp() throws BusyNicknameException, DatabaseException {
        Container container = GlobalTestState.container;
        encryptedKeyRepo = container.get(EncryptedKeyRepository.class);
        MemberRepository memberRepo = container.get(MemberRepository.class);
        jpaUtil = container.get(SimpleJPAUtil.class);

        sender = memberRepo.create("sender_encrypted_keys", "hash");
        receiver = memberRepo.create("receiver_encrypted_keys", "hash");

        EncryptionManager.registerAll();
    }

    @Test
    void testCreateAndFindEncryptedKeyEntity() throws DatabaseException {
        String encrypted = "encrypted-data";

        EncryptedKeyEntity created = encryptedKeyRepo.create(sender, receiver, encrypted);
        assertNotNull(created);
        assertNotNull(created.id());

        Optional<EncryptedKeyEntity> found = jpaUtil.find(EncryptedKeyEntity.class, created.id());
        assertTrue(found.isPresent());
        assertEquals(encrypted, found.get().encryptedKey());
    }

    @Test
    void testFindMissingEncryptedKeyEntity() throws DatabaseException {
        UUID id = UUID.randomUUID();
        Optional<EncryptedKeyEntity> result = jpaUtil.find(EncryptedKeyEntity.class, id);
        assertTrue(result.isEmpty());
    }
}
