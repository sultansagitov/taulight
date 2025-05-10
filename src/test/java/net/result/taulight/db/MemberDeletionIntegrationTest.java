package net.result.taulight.db;

import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.db.MemberRepository;
import net.result.sandnode.security.PasswordHashers;
import net.result.sandnode.util.Container;
import net.result.taulight.dto.ChatMessageInputDTO;
import org.junit.jupiter.api.*;

import java.time.ZonedDateTime;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

public class MemberDeletionIntegrationTest {

    private static MemberRepository memberRepo;
    private static DialogRepository dialogRepo;
    private static ChannelRepository channelRepo;
    private static MessageRepository messageRepo;
    private static InviteCodeRepository inviteCodeRepo;
    private static ReactionPackageRepository reactionPackageRepo;
    private static ReactionTypeRepository reactionTypeRepo;
    private static ReactionEntryRepository reactionEntryRepo;

    @BeforeAll
    public static void setup() {
        JPAUtil.buildEntityManagerFactory();
        Container container = new Container();
        memberRepo = container.get(MemberRepository.class);
        dialogRepo = container.get(DialogRepository.class);
        channelRepo = container.get(ChannelRepository.class);
        messageRepo = container.get(MessageRepository.class);
        inviteCodeRepo = container.get(InviteCodeRepository.class);
        reactionPackageRepo = container.get(ReactionPackageRepository.class);
        reactionTypeRepo = container.get(ReactionTypeRepository.class);
        reactionEntryRepo = container.get(ReactionEntryRepository.class);
    }

    @Test
    public void testDeleteMemberAndCheckDialogCleanup() throws Exception {
        MemberEntity m1 = memberRepo.create("alice", PasswordHashers.BCRYPT.hash("pass1", 12));
        MemberEntity m2 = memberRepo.create("bob", PasswordHashers.BCRYPT.hash("pass2", 12));

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
        MemberEntity member = memberRepo.create("charlie", PasswordHashers.BCRYPT.hash("123", 12));
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
        MemberEntity m1 = memberRepo.create("eva", PasswordHashers.BCRYPT.hash("123", 12));
        MemberEntity m2 = memberRepo.create("oliver", PasswordHashers.BCRYPT.hash("456", 12));
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

        ReactionPackageEntity emoji = reactionPackageRepo.create("emoji", "");
        ReactionTypeEntity like = reactionTypeRepo.create("Like", emoji);
        reactionEntryRepo.create(tau2, msg, like);

        assertTrue(reactionEntryRepo.delete(msg, tau2, like));

        reactionEntryRepo.create(tau2, msg, like);
        boolean deleted = memberRepo.delete(m2);

        assertTrue(deleted);
        assertTrue(reactionEntryRepo.delete(msg, tau2, like));
    }

    @Test
    public void testDeleteMemberAndCheckInviteCleanup() throws Exception {
        MemberEntity owner = memberRepo.create("sam", PasswordHashers.BCRYPT.hash("123", 12));
        MemberEntity invited = memberRepo.create("jack", PasswordHashers.BCRYPT.hash("456", 12));
        TauMemberEntity tauOwner = owner.tauMember();
        TauMemberEntity tauInvited = invited.tauMember();

        ChannelEntity channel = channelRepo.create("private", tauOwner);
        ZonedDateTime expiresDate = ZonedDateTime.now().plusDays(1);
        InviteCodeEntity invite = inviteCodeRepo.create(channel, tauInvited, tauOwner, expiresDate);

        assertTrue(inviteCodeRepo.find(invite.code()).isPresent());

        boolean deleted = memberRepo.delete(invited);

        assertTrue(deleted);
        assertTrue(inviteCodeRepo.find(invite.code()).isPresent());
    }
}
