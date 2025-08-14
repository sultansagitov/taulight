package net.result.sandnode.repository;

import net.result.sandnode.GlobalTestState;
import net.result.sandnode.encryption.AsymmetricEncryptions;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.entity.KeyStorageEntity;
import net.result.sandnode.entity.LoginEntity;
import net.result.sandnode.entity.MemberEntity;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.crypto.CannotUseEncryption;
import net.result.sandnode.exception.error.BusyNicknameException;
import net.result.sandnode.util.Container;
import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.db.SimpleJPAUtil;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class LoginsTest {

    private static LoginRepository loginRepo;
    private static MemberEntity member;
    private static KeyStorageRepository keyStorageRepo;
    private static JPAUtil jpaUtil;

    @BeforeAll
    static void setup() throws BusyNicknameException, DatabaseException {
        EncryptionManager.registerAll();

        Container container = GlobalTestState.container;
        loginRepo = container.get(LoginRepository.class);
        keyStorageRepo = container.get(KeyStorageRepository.class);
        jpaUtil = container.get(SimpleJPAUtil.class);

        MemberRepository memberRepo = container.get(MemberRepository.class);
        member = memberRepo.create("user_login_test", "hash");
    }

    @Test
    void testCreateAndFindLogin() throws DatabaseException, CannotUseEncryption {
        String ip = "192.168.0.1";
        String device = "Firefox on Linux";

        KeyStorageEntity keyStorageEntity = keyStorageRepo.create(AsymmetricEncryptions.ECIES.generate());

        LoginEntity created = loginRepo.create(member, keyStorageEntity, ip, device);
        assertNotNull(created);
        assertNotNull(created.id());
        assertEquals(ip, created.ip());
        assertEquals(device, created.device());

        Optional<LoginEntity> found = jpaUtil.find(LoginEntity.class, created.id());
        assertTrue(found.isPresent());
        assertEquals(created.id(), found.get().id());
        assertEquals(ip, found.get().ip());
    }

    @Test
    void testCreateLoginWithLoginLink() throws DatabaseException, CannotUseEncryption {
        KeyStorageEntity keyStorageEntity = keyStorageRepo.create(AsymmetricEncryptions.ECIES.generate());

        LoginEntity baseLogin = loginRepo.create(member, keyStorageEntity, "10.0.0.1", "Mac");
        LoginEntity linkedLogin = loginRepo.create(baseLogin, "10.0.0.2");

        assertNotNull(linkedLogin);
        assertNotNull(linkedLogin.id());
        assertEquals(baseLogin.id(), linkedLogin.login().id());

        Optional<LoginEntity> found = jpaUtil.find(LoginEntity.class, linkedLogin.id());
        assertTrue(found.isPresent());
        assertEquals(baseLogin.id(), found.get().login().id());
    }

    @Test
    void testFindLoginsByDevice() throws DatabaseException, CannotUseEncryption {
        KeyStorageEntity keyStorageEntity = keyStorageRepo.create(AsymmetricEncryptions.ECIES.generate());

        loginRepo.create(member, keyStorageEntity, "172.16.0.1", "Device-A");
        loginRepo.create(member, keyStorageEntity, "172.16.0.2", "Device-B");

        List<LoginEntity> deviceLogins = loginRepo.byDevice(member);
        assertNotNull(deviceLogins);
        assertFalse(deviceLogins.isEmpty());

        for (LoginEntity login : deviceLogins) {
            assertEquals(member.id(), login.member().id());
            assertNull(login.login());
        }
    }

    @Test
    void testFindMissingLogin() throws DatabaseException {
        UUID randomId = UUID.randomUUID();
        Optional<LoginEntity> result = jpaUtil.find(LoginEntity.class, randomId);
        assertTrue(result.isEmpty());
    }
}
