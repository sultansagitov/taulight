package net.result.taulight.db;

import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.security.PasswordHashers;
import net.result.taulight.dto.ChatMessageInputDTO;
import org.junit.jupiter.api.*;

import java.time.ZonedDateTime;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

public class MemberDeletionIntegrationTest {

    private static TauDatabase database;

    @BeforeAll
    public static void setup() {
        JPAUtil.buildEntityManagerFactory();
        database = new TauJPADatabase(PasswordHashers.BCRYPT);
    }

    @Test
    public void testDeleteMemberAndCheckDialogCleanup() throws Exception {
        MemberEntity m1 = database.registerMember("alice", "pass1");
        MemberEntity m2 = database.registerMember("bob", "pass2");

        TauMemberEntity tau1 = m1.tauMember();
        TauMemberEntity tau2 = m2.tauMember();

        database.createDialog(tau1, tau2);

        assertTrue(database.findDialog(tau1, tau2).isPresent());

        database.deleteMember(m1);

        assertTrue(database.findDialog(tau1, tau2).isPresent());
    }

    @Test
    public void testDeleteMemberAndCheckMessageCleanup() throws Exception {
        MemberEntity member = database.registerMember("charlie", "123");
        TauMemberEntity tau = member.tauMember();

        ChatEntity chat = database.createChannel("general", tau);
        ChatMessageInputDTO input = new ChatMessageInputDTO()
                .setContent("Hello world")
                .setChat(chat)
                .setMember(member)
                .setSentDatetimeNow()
                .setRepliedToMessages(new HashSet<>())
                .setSys(true);
        database.createMessage(chat, input, tau);

        assertEquals(1, database.getMessageCount(chat));

        database.deleteMember(member);

        assertEquals(1, database.getMessageCount(chat));
    }

    @Test
    public void testDeleteMemberAndCheckReactionCleanup() throws Exception {
        MemberEntity m1 = database.registerMember("eva", "123");
        MemberEntity m2 = database.registerMember("oliver", "456");
        TauMemberEntity tau1 = m1.tauMember();
        TauMemberEntity tau2 = m2.tauMember();

        ChatEntity chat = database.createChannel("fun", tau1);
        ChatMessageInputDTO input = new ChatMessageInputDTO()
                .setContent("Hello world")
                .setChat(chat)
                .setMember(m1)
                .setSentDatetimeNow()
                .setRepliedToMessages(new HashSet<>())
                .setSys(true);
        MessageEntity msg = database.createMessage(chat, input, tau1);

        ReactionTypeEntity like = database.createReactionType("Like", "emoji");
        database.createReactionEntry(tau2, msg, like);

        assertTrue(database.removeReactionEntry(msg, tau2, like));

        database.createReactionEntry(tau2, msg, like);
        database.deleteMember(m2);

        assertTrue(database.removeReactionEntry(msg, tau2, like));
    }

    @Test
    public void testDeleteMemberAndCheckInviteCleanup() throws Exception {
        MemberEntity owner = database.registerMember("sam", "123");
        MemberEntity invited = database.registerMember("jack", "456");
        TauMemberEntity tauOwner = owner.tauMember();
        TauMemberEntity tauInvited = invited.tauMember();

        ChannelEntity channel = database.createChannel("private", tauOwner);
        InviteCodeEntity invite = database.createInviteCode(channel, tauInvited, tauOwner, ZonedDateTime.now().plusDays(1));

        assertTrue(database.findInviteCode(invite.code()).isPresent());

        database.deleteMember(invited);

        assertTrue(database.findInviteCode(invite.code()).isPresent());
    }
}
