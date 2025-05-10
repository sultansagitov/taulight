package net.result.taulight.db;

import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.db.MemberRepository;
import net.result.sandnode.security.PasswordHashers;
import net.result.taulight.dto.ChatMessageInputDTO;
import org.junit.jupiter.api.*;

import java.time.ZonedDateTime;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

public class MemberDeletionIntegrationTest {

    private static TauDatabase database;
    private static MemberRepository memberRepo;
    private static DialogRepository dialogRepo;
    private static ChannelRepository channelRepo;
    private static MessageRepository messageRepo;

    @BeforeAll
    public static void setup() {
        JPAUtil.buildEntityManagerFactory();
        database = new TauJPADatabase(PasswordHashers.BCRYPT);
        memberRepo = new MemberRepository();
        dialogRepo = new DialogRepository();
        channelRepo = new ChannelRepository();
        messageRepo = new MessageRepository();
    }

    @Test
    public void testDeleteMemberAndCheckDialogCleanup() throws Exception {
        MemberEntity m1 = database.registerMember("alice", "pass1");
        MemberEntity m2 = database.registerMember("bob", "pass2");

        TauMemberEntity tau1 = m1.tauMember();
        TauMemberEntity tau2 = m2.tauMember();

        dialogRepo.create(tau1, tau2);

        assertTrue(dialogRepo.findByMembers(tau1, tau2).isPresent());

        boolean deleted = memberRepo.delete(m1);

        assertTrue(deleted);
        assertTrue(dialogRepo.findByMembers(tau1, tau2).isPresent());
    }

    @Test
    public void testDeleteMemberAndCheckMessageCleanup() throws Exception {
        MemberEntity member = database.registerMember("charlie", "123");
        TauMemberEntity tau = member.tauMember();

        ChatEntity chat = channelRepo.create("general", tau);
        ChatMessageInputDTO input = new ChatMessageInputDTO()
                .setContent("Hello world")
                .setChat(chat)
                .setMember(member)
                .setSentDatetimeNow()
                .setRepliedToMessages(new HashSet<>())
                .setSys(true);
        messageRepo.create(chat, input, tau);

        assertEquals(1, messageRepo.countMessagesByChat(chat));

        boolean deleted = memberRepo.delete(member);

        assertTrue(deleted);
        assertEquals(1, messageRepo.countMessagesByChat(chat));
    }

    @Test
    public void testDeleteMemberAndCheckReactionCleanup() throws Exception {
        MemberEntity m1 = database.registerMember("eva", "123");
        MemberEntity m2 = database.registerMember("oliver", "456");
        TauMemberEntity tau1 = m1.tauMember();
        TauMemberEntity tau2 = m2.tauMember();

        ChatEntity chat = channelRepo.create("fun", tau1);
        ChatMessageInputDTO input = new ChatMessageInputDTO()
                .setContent("Hello world")
                .setChat(chat)
                .setMember(m1)
                .setSentDatetimeNow()
                .setRepliedToMessages(new HashSet<>())
                .setSys(true);
        MessageEntity msg = messageRepo.create(chat, input, tau1);

        ReactionPackageEntity emoji = database.createReactionPackage("emoji", "");
        ReactionTypeEntity like = database.createReactionType("Like", emoji);
        database.createReactionEntry(tau2, msg, like);

        assertTrue(database.removeReactionEntry(msg, tau2, like));

        database.createReactionEntry(tau2, msg, like);
        boolean deleted = memberRepo.delete(m2);

        assertTrue(deleted);
        assertTrue(database.removeReactionEntry(msg, tau2, like));
    }

    @Test
    public void testDeleteMemberAndCheckInviteCleanup() throws Exception {
        MemberEntity owner = database.registerMember("sam", "123");
        MemberEntity invited = database.registerMember("jack", "456");
        TauMemberEntity tauOwner = owner.tauMember();
        TauMemberEntity tauInvited = invited.tauMember();

        ChannelEntity channel = channelRepo.create("private", tauOwner);
        ZonedDateTime expiresDate = ZonedDateTime.now().plusDays(1);
        InviteCodeEntity invite = database.createInviteCode(channel, tauInvited, tauOwner, expiresDate);

        assertTrue(database.findInviteCode(invite.code()).isPresent());

        boolean deleted = memberRepo.delete(invited);

        assertTrue(deleted);
        assertTrue(database.findInviteCode(invite.code()).isPresent());
    }
}
