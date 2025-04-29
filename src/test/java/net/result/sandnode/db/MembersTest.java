package net.result.sandnode.db;

import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.BusyNicknameException;
import net.result.sandnode.security.PasswordHashers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MembersTest {

    private static Database database;

    @BeforeAll
    public static void setup() {
        JPAUtil.buildEntityManagerFactory();
        database = new JPADatabase(PasswordHashers.BCRYPT);
    }

    @Test
    public void registerMember() throws DatabaseException, BusyNicknameException {
        MemberEntity newMember = database.registerMember("testuser123", "securePass!");
        assertNotNull(newMember);
        assertEquals("testuser123", newMember.nickname());

        // Additional assertions
        assertNotNull(newMember.id());
        assertNotNull(newMember.tauMember());
        assertEquals(0, newMember.tauMember().dialogs().size(), "New member should have no dialogs");
        assertEquals(0, newMember.tauMember().channels().size(), "New member should have no channels");

        // Test duplicate nickname
        assertThrows(BusyNicknameException.class, () -> database.registerMember("testuser123", "securePass!"));
    }

    @Test
    public void findMemberByNickname() throws DatabaseException, BusyNicknameException {
        MemberEntity registeredMember = database.registerMember("nicksearch", "pass1234");

        Optional<MemberEntity> found = database.findMemberByNickname("nicksearch");
        assertTrue(found.isPresent());
        assertEquals("nicksearch", found.get().nickname());

        // Additional assertions
        assertEquals(registeredMember.id(), found.get().id(), "IDs should match");

        // Test non-existent nickname
        Optional<MemberEntity> notFound = database.findMemberByNickname("nonexistentuser");
        assertFalse(notFound.isPresent(), "Should not find non-existent user");
    }
}