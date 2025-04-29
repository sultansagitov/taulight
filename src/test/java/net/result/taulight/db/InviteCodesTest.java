package net.result.taulight.db;

import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.BusyNicknameException;
import net.result.sandnode.security.PasswordHashers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class InviteCodesTest {
    private static TauDatabase database;
    private static TauMemberEntity member1;
    private static TauMemberEntity member2;
    private static TauMemberEntity member3;
    private static TauMemberEntity member4;
    private static TauMemberEntity member5;
    private static TauMemberEntity member6;

    @BeforeAll
    public static void setup() throws DatabaseException, BusyNicknameException {
        JPAUtil.buildEntityManagerFactory();

        database = new TauJPADatabase(PasswordHashers.BCRYPT);

        member1 = database.registerMember("user1_invites", "password123").tauMember();
        member2 = database.registerMember("user2_invites", "password123").tauMember();
        member3 = database.registerMember("user3_invites", "password123").tauMember();
        member4 = database.registerMember("user4_invites", "password123").tauMember();
        member5 = database.registerMember("user5_invites", "password123").tauMember();
        member6 = database.registerMember("user6_invites", "password123").tauMember();

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
        ChannelEntity channel = database.createChannel("Test Channel", member1);

        ZonedDateTime expiresDate = ZonedDateTime.now().plusDays(1);
        InviteCodeEntity inviteCode = database.createInviteCode(channel, member2, member1, expiresDate);

        String stringCode = inviteCode.code();
        Optional<InviteCodeEntity> foundInviteCode = database.findInviteCode(stringCode);
        assertTrue(foundInviteCode.isPresent());
        assertEquals(stringCode, foundInviteCode.get().code());

        // Additional assertions
        assertNotNull(inviteCode.id(), "Invite code ID should not be null");
        assertEquals(channel, inviteCode.channel(), "Invite should be for the correct channel");
        assertEquals(member1, inviteCode.sender(), "Sender should be member1");
        assertEquals(member2, inviteCode.receiver(), "Receiver should be member2");
        assertEquals(expiresDate.toEpochSecond(), inviteCode.expiresDate().toEpochSecond(), "Expiration date should match");
        assertNull(inviteCode.activationDate(), "New invite code should not be activated");
    }

    @Test
    public void findInviteCode1() throws DatabaseException {
        ChannelEntity channel = database.createChannel("InviteChannel", member1);

        ZonedDateTime expiration = ZonedDateTime.now().plusDays(1);
        InviteCodeEntity invite = database.createInviteCode(channel, member2, member1, expiration);

        Optional<InviteCodeEntity> result = database.findInviteCode(invite.code());
        assertTrue(result.isPresent());
        assertEquals(invite, result.get());

        // Additional assertions
        assertEquals(invite.id(), result.get().id(), "IDs should match");
        assertEquals(invite.channel(), result.get().channel(), "Channels should match");
        assertEquals(invite.receiver(), result.get().receiver(), "Receivers should match");
        assertEquals(invite.sender(), result.get().sender(), "Senders should match");

        // Test with non-existent invite code
        Optional<InviteCodeEntity> nonExistentInvite = database.findInviteCode("non-existent-code");
        assertFalse(nonExistentInvite.isPresent(), "Should not find non-existent invite code");
    }

    @Test
    public void findInviteCode2() throws DatabaseException {
        ChannelEntity channel = database.createChannel("InviteChannel", member3);

        ZonedDateTime expiration = ZonedDateTime.now().plusDays(1);
        InviteCodeEntity invite1 = database.createInviteCode(channel, member4, member3, expiration);
        InviteCodeEntity invite2 = database.createInviteCode(channel, member4, member3, expiration);

        Collection<InviteCodeEntity> result = database.findInviteCode(channel, member4);
        assertEquals(2, result.size());

        // Additional assertions
        List<String> inviteCodes = result.stream().map(InviteCodeEntity::code).toList();
        assertTrue(inviteCodes.contains(invite1.code()), "Result should contain first invite code");
        assertTrue(inviteCodes.contains(invite2.code()), "Result should contain second invite code");

        // Test with a different receiver
        InviteCodeEntity invite3 = database.createInviteCode(channel, member5, member3, expiration);
        Collection<InviteCodeEntity> result2 = database.findInviteCode(channel, member5);
        assertEquals(1, result2.size(), "Should find only one invite from member5");
        assertEquals(invite3.code(), result2.iterator().next().code(), "Should find the correct invite");

        // Test with no invites
        Collection<InviteCodeEntity> emptyResult = database.findInviteCode(channel, member6);
        assertEquals(0, emptyResult.size(), "Should find no invites for member6");
    }

    @Test
    public void activateInviteCode() throws DatabaseException {
        ChannelEntity channel = database.createChannel("Test Channel", member1);

        ZonedDateTime expiresDate = ZonedDateTime.now().plusDays(1);
        InviteCodeEntity inviteCode = database.createInviteCode(channel, member1, member2, expiresDate);

        boolean activated = database.activateInviteCode(inviteCode);
        assertTrue(activated);

        // Additional assertions
        Optional<InviteCodeEntity> found = database.findInviteCode(inviteCode.code());
        assertTrue(found.isPresent(), "Should still find the invite code after activation");
        assertNotNull(found.get().activationDate(), "Invite code should be marked as activated");

        // Test activating an already activated code
        boolean activatedAgain = database.activateInviteCode(inviteCode);
        assertFalse(activatedAgain, "Should not activate an already activated invite code");

        // Test with an expired invite code
        ZonedDateTime pastDate = ZonedDateTime.now().minusDays(1);
        InviteCodeEntity expiredInvite = database.createInviteCode(channel, member1, member3, pastDate);
        boolean activatedExpired = database.activateInviteCode(expiredInvite);
        assertFalse(activatedExpired, "Should not activate an expired invite code");
    }

}
