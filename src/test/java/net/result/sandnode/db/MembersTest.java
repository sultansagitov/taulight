package net.result.sandnode.db;

import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.BusyNicknameException;
import net.result.sandnode.security.PasswordHashers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MembersTest {

    private static MemberRepository memberRepo;

    @BeforeAll
    public static void setup() {
        JPAUtil.buildEntityManagerFactory();
        memberRepo = new MemberRepository();
    }

    @Test
    public void registerMember() throws DatabaseException, BusyNicknameException {
        MemberEntity newMember = memberRepo.create("testuser123", PasswordHashers.BCRYPT.hash("securePass!", 12));
        assertNotNull(newMember);
        assertEquals("testuser123", newMember.nickname());

        // Additional assertions
        assertNotNull(newMember.id());
        assertNotNull(newMember.tauMember());
        assertEquals(0, newMember.tauMember().dialogs().size(), "New member should have no dialogs");
        assertEquals(0, newMember.tauMember().channels().size(), "New member should have no channels");

        // Test duplicate nickname
        assertThrows(BusyNicknameException.class, () -> memberRepo.create("testuser123", PasswordHashers.BCRYPT.hash("securePass!", 12)));
    }

    @Test
    public void findMemberByNickname() throws DatabaseException, BusyNicknameException {
        MemberEntity registeredMember = memberRepo.create("nicksearch", PasswordHashers.BCRYPT.hash("pass1234", 12));

        Optional<MemberEntity> found = memberRepo.findByNickname("nicksearch");
        assertTrue(found.isPresent());
        assertEquals("nicksearch", found.get().nickname());

        // Additional assertions
        assertEquals(registeredMember.id(), found.get().id(), "IDs should match");

        // Test non-existent nickname
        Optional<MemberEntity> notFound = memberRepo.findByNickname("nonexistentuser");
        assertFalse(notFound.isPresent(), "Should not find non-existent user");
    }
}