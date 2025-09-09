package net.result.taulight.repository;

import net.result.sandnode.GlobalTestState;
import net.result.sandnode.exception.error.NoEffectException;
import net.result.sandnode.repository.MemberRepository;
import net.result.sandnode.util.Container;
import net.result.taulight.db.TauMemberCreationListener;
import net.result.taulight.entity.GroupEntity;
import net.result.taulight.entity.InviteCodeEntity;
import net.result.taulight.entity.TauMemberEntity;
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
    public static void setup() {
        Container container = GlobalTestState.container;

        container.addInstanceItem(TauMemberCreationListener.class);

        MemberRepository memberRepo = container.get(MemberRepository.class);
        groupRepo = container.get(GroupRepository.class);
        inviteCodeRepo = container.get(InviteCodeRepository.class);

        member1 = memberRepo.create("user1_invites", "hash").getTauMember();
        member2 = memberRepo.create("user2_invites", "hash").getTauMember();
        member3 = memberRepo.create("user3_invites", "hash").getTauMember();
        member4 = memberRepo.create("user4_invites", "hash").getTauMember();
        member5 = memberRepo.create("user5_invites", "hash").getTauMember();
        member6 = memberRepo.create("user6_invites", "hash").getTauMember();

        // Assert that all members are properly created
        assertNotNull(member1.id());
        assertNotNull(member2.id());
        assertNotNull(member3.id());
        assertNotNull(member4.id());
        assertNotNull(member5.id());
        assertNotNull(member6.id());
    }

    @Test
    public void createInviteCode() {
        GroupEntity group = groupRepo.create("Test Group", member1);

        ZonedDateTime expiresDate = ZonedDateTime.now().plusDays(1);
        InviteCodeEntity inviteCode = inviteCodeRepo.create(group, member2, member1, expiresDate);

        String stringCode = inviteCode.getCode();
        Optional<InviteCodeEntity> foundInviteCode = inviteCodeRepo.find(stringCode);
        assertTrue(foundInviteCode.isPresent());
        assertEquals(stringCode, foundInviteCode.get().getCode());

        // Additional assertions
        assertNotNull(inviteCode.id(), "Invite code ID should not be null");
        assertEquals(group, inviteCode.getGroup(), "Invite should be for the correct group");
        assertEquals(member1, inviteCode.getSender(), "Sender should be member1");
        assertEquals(member2, inviteCode.getReceiver(), "Receiver should be member2");
        assertEquals(expiresDate.toEpochSecond(), inviteCode.getExpiresDate().toEpochSecond(),
                "Expiration date should match");
        assertNull(inviteCode.getActivatedAt(), "New invite code should not be activated");
    }

    @Test
    public void findInviteCode1() {
        GroupEntity group = groupRepo.create("InviteGroup", member1);

        ZonedDateTime expiration = ZonedDateTime.now().plusDays(1);
        InviteCodeEntity invite = inviteCodeRepo.create(group, member2, member1, expiration);

        Optional<InviteCodeEntity> result = inviteCodeRepo.find(invite.getCode());
        assertTrue(result.isPresent());
        assertEquals(invite, result.get());

        // Additional assertions
        assertEquals(invite.id(), result.get().id(), "IDs should match");
        assertEquals(invite.getGroup(), result.get().getGroup(), "Groups should match");
        assertEquals(invite.getReceiver(), result.get().getReceiver(), "Receivers should match");
        assertEquals(invite.getSender(), result.get().getSender(), "Senders should match");

        // Test with non-existent invite code
        Optional<InviteCodeEntity> nonExistentInvite = inviteCodeRepo.find("non-existent-code");
        assertFalse(nonExistentInvite.isPresent(), "Should not find non-existent invite code");
    }

    @Test
    public void findInviteCode2() {
        GroupEntity group = groupRepo.create("InviteGroup", member3);

        ZonedDateTime expiration = ZonedDateTime.now().plusDays(1);
        InviteCodeEntity invite1 = inviteCodeRepo.create(group, member4, member3, expiration);
        InviteCodeEntity invite2 = inviteCodeRepo.create(group, member4, member3, expiration);

        Collection<InviteCodeEntity> result = inviteCodeRepo.find(group, member4);
        assertEquals(2, result.size());

        // Additional assertions
        List<String> inviteCodes = result.stream().map(InviteCodeEntity::getCode).toList();
        assertTrue(inviteCodes.contains(invite1.getCode()), "Result should contain first invite code");
        assertTrue(inviteCodes.contains(invite2.getCode()), "Result should contain second invite code");

        // Test with a different receiver
        InviteCodeEntity invite3 = inviteCodeRepo.create(group, member5, member3, expiration);
        Collection<InviteCodeEntity> result2 = inviteCodeRepo.find(group, member5);
        assertEquals(1, result2.size(), "Should find only one invite from member5");
        assertEquals(invite3.getCode(), result2.iterator().next().getCode(), "Should find the correct invite");

        // Test with no invites
        Collection<InviteCodeEntity> emptyResult = inviteCodeRepo.find(group, member6);
        assertEquals(0, emptyResult.size(), "Should find no invites for member6");
    }

    @Test
    public void activateInviteCode() {
        GroupEntity group = groupRepo.create("Test Group", member1);

        ZonedDateTime expiresDate = ZonedDateTime.now().plusDays(1);
        InviteCodeEntity inviteCode = inviteCodeRepo.create(group, member1, member2, expiresDate);

        inviteCodeRepo.activate(inviteCode);

        // Additional assertions
        Optional<InviteCodeEntity> found = inviteCodeRepo.find(inviteCode.getCode());
        assertTrue(found.isPresent(), "Should still find the invite code after activation");
        assertNotNull(found.get().getActivatedAt(), "Invite code should be marked as activated");

        // Test activating an already activated code
        assertThrows(NoEffectException.class, () -> inviteCodeRepo.activate(inviteCode));

        // Test with an expired invite code
        ZonedDateTime pastDate = ZonedDateTime.now().minusDays(1);
        InviteCodeEntity expiredInvite = inviteCodeRepo.create(group, member1, member3, pastDate);
        assertThrows(NoEffectException.class, () -> inviteCodeRepo.activate(expiredInvite));
    }
}
