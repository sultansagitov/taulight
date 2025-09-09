package net.result.taulight.repository;

import net.result.sandnode.GlobalTestState;
import net.result.sandnode.encryption.AsymmetricEncryptions;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.entity.MemberEntity;
import net.result.sandnode.exception.error.BusyNicknameException;
import net.result.sandnode.repository.MemberRepository;
import net.result.sandnode.util.Container;
import net.result.taulight.db.TauMemberCreationListener;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TauMembersTest {

    private static MemberRepository memberRepo;

    @BeforeAll
    public static void setup() {
        Container container = GlobalTestState.container;

        container.addInstanceItem(TauMemberCreationListener.class);

        memberRepo = container.get(MemberRepository.class);

        EncryptionManager.registerAll();
    }

    @Test
    public void registerMember() {
        MemberEntity newMember = memberRepo.create("tau_testuser123", "hash");
        assertNotNull(newMember);
        assertEquals("tau_testuser123", newMember.getNickname());

        // Additional assertions
        assertNotNull(newMember.id());
        assertNotNull(newMember.getTauMember());
        assertEquals(0, newMember.getTauMember().getDialogs().size(), "New member should have no dialogs");
        assertEquals(0, newMember.getTauMember().getGroups().size(), "New member should have no groups");

        // Test duplicate nickname
        assertThrows(BusyNicknameException.class, () -> memberRepo.create("tau_testuser123", "hash"));
    }

    @Test
    public void registerMemberWithKeyStorage() {
        AsymmetricKeyStorage keyStorage = AsymmetricEncryptions.ECIES.generate();
        MemberEntity newMember = memberRepo.create("tau_testuser123_with_key", "hash", keyStorage);
        assertNotNull(newMember);
        assertEquals("tau_testuser123_with_key", newMember.getNickname());

        // Additional assertions
        assertNotNull(newMember.id());
        assertNotNull(newMember.getTauMember());
        assertEquals(0, newMember.getTauMember().getDialogs().size(), "New member should have no dialogs");
        assertEquals(0, newMember.getTauMember().getGroups().size(), "New member should have no groups");

        // Test duplicate nickname
        assertThrows(BusyNicknameException.class, () -> memberRepo.create("tau_testuser123", "hash"));
    }

    @Test
    public void findMemberByNickname() {
        MemberEntity registeredMember = memberRepo.create("tau_nicksearch", "hash");

        Optional<MemberEntity> found = memberRepo.findByNickname("tau_nicksearch");
        assertTrue(found.isPresent());
        assertEquals("tau_nicksearch", found.get().getNickname());

        // Additional assertions
        assertEquals(registeredMember.id(), found.get().id(), "IDs should match");

        // Test non-existent nickname
        Optional<MemberEntity> notFound = memberRepo.findByNickname("nonexistentuser");
        assertFalse(notFound.isPresent(), "Should not find non-existent user");
    }
}