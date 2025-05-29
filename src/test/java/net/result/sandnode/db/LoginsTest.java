package net.result.sandnode.db;

import net.result.sandnode.GlobalTestState;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.BusyNicknameException;
import net.result.sandnode.util.Container;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class LoginsTest {

    private static LoginRepository loginRepo;
    private static MemberEntity member;

    @BeforeAll
    static void setup() throws BusyNicknameException, DatabaseException {
        Container container = GlobalTestState.container;
        loginRepo = container.get(LoginRepository.class);
        MemberRepository memberRepo = container.get(MemberRepository.class);
        member = memberRepo.create("user_login_test", "hash");
    }

    @Test
    void testCreateAndFindLogin() throws DatabaseException {
        String ip = "192.168.0.1";
        String device = "Firefox on Linux";

        LoginEntity created = loginRepo.create(member, ip, device);
        assertNotNull(created);
        assertNotNull(created.id());
        assertEquals(ip, created.ip());
        assertEquals(device, created.device());

        Optional<LoginEntity> found = loginRepo.find(created.id());
        assertTrue(found.isPresent());
        assertEquals(created.id(), found.get().id());
        assertEquals(ip, found.get().ip());
    }

    @Test
    void testCreateLoginWithLoginLink() throws DatabaseException {
        LoginEntity baseLogin = loginRepo.create(member, "10.0.0.1", "Mac");
        LoginEntity linkedLogin = loginRepo.create(baseLogin, "10.0.0.2");

        assertNotNull(linkedLogin);
        assertNotNull(linkedLogin.id());
        assertEquals(baseLogin.id(), linkedLogin.login().id());

        Optional<LoginEntity> found = loginRepo.find(linkedLogin.id());
        assertTrue(found.isPresent());
        assertEquals(baseLogin.id(), found.get().login().id());
    }

    @Test
    void testFindLoginsByDevice() throws DatabaseException {
        loginRepo.create(member, "172.16.0.1", "Device-A");
        loginRepo.create(member, "172.16.0.2", "Device-B");

        List<LoginEntity> deviceLogins = loginRepo.byDevice(member);
        assertNotNull(deviceLogins);
        assertFalse(deviceLogins.isEmpty());

        for (LoginEntity login : deviceLogins) {
            assertEquals(member.id(), login.member().id());
            assertNull(login.login());
        }
    }

    @Test
    void testFindMissingLogin() {
        UUID randomId = UUID.randomUUID();
        Optional<LoginEntity> result = loginRepo.find(randomId);
        assertTrue(result.isEmpty());
    }
}
