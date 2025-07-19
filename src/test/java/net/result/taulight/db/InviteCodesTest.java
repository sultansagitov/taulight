package net.result.taulight.db;

import net.result.sandnode.GlobalTestState;
import net.result.sandnode.db.MemberRepository;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.BusyNicknameException;
import net.result.sandnode.util.Container;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class InviteCodesTest {
    private static TauMemberEntity member1;
    private static TauMemberEntity member2;
    private static TauMemberEntity member3;
    private static TauMemberEntity member4;
    private static TauMemberEntity member5;
    private static TauMemberEntity member6;
    private static GroupRepository groupRepo;
    private static InviteCodeRepository inviteCodeRepo;

    @BeforeAll
    public static void setup() throws DatabaseException, BusyNicknameException {
        Container container = GlobalTestState.container;
        MemberRepository memberRepo = container.get(MemberRepository.class);
        groupRepo = container.get(GroupRepository.class);
        inviteCodeRepo = container.get(InviteCodeRepository.class);

        member1 = memberRepo.create("user1_invites", "hash").tauMember();
        member2 = memberRepo.create("user2_invites", "hash").tauMember();
        member3 = memberRepo.create("user3_invites", "hash").tauMember();
        member4 = memberRepo.create("user4_invites", "hash").tauMember();
        member5 = memberRepo.create("user5_invites", "hash").tauMember();
        member6 = memberRepo.create("user6_invites", "hash").tauMember();

        // Assert that all members are properly created
        assertNotNull(member1.id());
        assertNotNull(member2.id());
        assertNotNull(member3.id());
        assertNotNull(member4.id());
        assertNotNull(member5.id());
        assertNotNull(member6.id());
    }

    @Test
    public void createInviteCode() throws DatabaseException {
        GroupEntity group = groupRepo.create("Test Group", member1);

        ZonedDateTime expiresDate = ZonedDateTime.now().plusDays(1);
        InviteCodeEntity inviteCode = inviteCodeRepo.create(group, member2, member1, expiresDate);

        String stringCode = inviteCode.code();
        Optional<InviteCodeEntity> foundInviteCode = inviteCodeRepo.find(stringCode);
        assertTrue(foundInviteCode.isPresent());
        assertEquals(stringCode, foundInviteCode.get().code());

        // Additional assertions
        assertNotNull(inviteCode.id(), "Invite code ID should not be null");
        assertEquals(group, inviteCode.group(), "Invite should be for the correct group");
        assertEquals(member1, inviteCode.sender(), "Sender should be member1");
        assertEquals(member2, inviteCode.receiver(), "Receiver should be member2");
        assertEquals(expiresDate.toEpochSecond(), inviteCode.expiresDate().toEpochSecond(),
                "Expiration date should match");
        assertNull(inviteCode.activationDate(), "New invite code should not be activated");
    }

    @Test
    public void findInviteCode1() throws DatabaseException {
        GroupEntity group = groupRepo.create("InviteGroup", member1);

        ZonedDateTime expiration = ZonedDateTime.now().plusDays(1);
        InviteCodeEntity invite = inviteCodeRepo.create(group, member2, member1, expiration);

        Optional<InviteCodeEntity> result = inviteCodeRepo.find(invite.code());
        assertTrue(result.isPresent());
        assertEquals(invite, result.get());

        // Additional assertions
        assertEquals(invite.id(), result.get().id(), "IDs should match");
        assertEquals(invite.group(), result.get().group(), "Groups should match");
        assertEquals(invite.receiver(), result.get().receiver(), "Receivers should match");
        assertEquals(invite.sender(), result.get().sender(), "Senders should match");

        // Test with non-existent invite code
        Optional<InviteCodeEntity> nonExistentInvite = inviteCodeRepo.find("non-existent-code");
        assertFalse(nonExistentInvite.isPresent(), "Should not find non-existent invite code");
    }

    @Test
    public void findInviteCode2() throws DatabaseException {
        GroupEntity group = groupRepo.create("InviteGroup", member3);

        ZonedDateTime expiration = ZonedDateTime.now().plusDays(1);
        InviteCodeEntity invite1 = inviteCodeRepo.create(group, member4, member3, expiration);
        InviteCodeEntity invite2 = inviteCodeRepo.create(group, member4, member3, expiration);

        Collection<InviteCodeEntity> result = inviteCodeRepo.find(group, member4);
        assertEquals(2, result.size());

        // Additional assertions
        List<String> inviteCodes = result.stream().map(InviteCodeEntity::code).toList();
        assertTrue(inviteCodes.contains(invite1.code()), "Result should contain first invite code");
        assertTrue(inviteCodes.contains(invite2.code()), "Result should contain second invite code");

        // Test with a different receiver
        InviteCodeEntity invite3 = inviteCodeRepo.create(group, member5, member3, expiration);
        Collection<InviteCodeEntity> result2 = inviteCodeRepo.find(group, member5);
        assertEquals(1, result2.size(), "Should find only one invite from member5");
        assertEquals(invite3.code(), result2.iterator().next().code(), "Should find the correct invite");

        // Test with no invites
        Collection<InviteCodeEntity> emptyResult = inviteCodeRepo.find(group, member6);
        assertEquals(0, emptyResult.size(), "Should find no invites for member6");
    }

    @Test
    public void activateInviteCode() throws DatabaseException {
        GroupEntity group = groupRepo.create("Test Group", member1);

        ZonedDateTime expiresDate = ZonedDateTime.now().plusDays(1);
        InviteCodeEntity inviteCode = inviteCodeRepo.create(group, member1, member2, expiresDate);

        boolean activated = inviteCodeRepo.activate(inviteCode);
        assertTrue(activated);

        // Additional assertions
        Optional<InviteCodeEntity> found = inviteCodeRepo.find(inviteCode.code());
        assertTrue(found.isPresent(), "Should still find the invite code after activation");
        assertNotNull(found.get().activationDate(), "Invite code should be marked as activated");

        // Test activating an already activated code
        boolean activatedAgain = inviteCodeRepo.activate(inviteCode);
        assertFalse(activatedAgain, "Should not activate an already activated invite code");

        // Test with an expired invite code
        ZonedDateTime pastDate = ZonedDateTime.now().minusDays(1);
        InviteCodeEntity expiredInvite = inviteCodeRepo.create(group, member1, member3, pastDate);
        boolean activatedExpired = inviteCodeRepo.activate(expiredInvite);
        assertFalse(activatedExpired, "Should not activate an expired invite code");
    }
}
