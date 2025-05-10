package net.result.taulight.db;

import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.db.MemberRepository;
import net.result.sandnode.security.PasswordHashers;
import net.result.taulight.dto.ChatMessageInputDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

public class CascadingTest {
    private static MemberRepository memberRepo;
    private static ChannelRepository channelRepo;
    private static MessageRepository messageRepo;
    private static InviteCodeRepository inviteCodeRepo;
    private static ReactionPackageRepository reactionPackageRepo;
    private static ReactionTypeRepository reactionTypeRepo;
    private static ReactionEntryRepository reactionEntryRepo;

    @BeforeAll
    public static void setup() {
        JPAUtil.buildEntityManagerFactory();
        memberRepo = new MemberRepository();
        channelRepo = new ChannelRepository();
        messageRepo = new MessageRepository();
        inviteCodeRepo = new InviteCodeRepository();
        reactionPackageRepo = new ReactionPackageRepository();
        reactionTypeRepo = new ReactionTypeRepository();
        reactionEntryRepo = new ReactionEntryRepository();
    }

    @Test
    public void testLeaveFromChannel() throws Exception {
        MemberEntity m1 = memberRepo.create("new_user", PasswordHashers.BCRYPT.hash("pass", 12));
        TauMemberEntity tau = m1.tauMember();

        ChannelEntity channel = channelRepo.create("news", tau);

        boolean added = channelRepo.addMember(channel, tau);
        assertFalse(added);

        boolean left = channelRepo.removeMember(channel, tau);
        assertTrue(left);

        boolean leftAgain = channelRepo.removeMember(channel, tau);
        assertFalse(leftAgain);
    }

    @Test
    public void testRemoveReactionByObject() throws Exception {
        MemberEntity m1 = memberRepo.create("reacter", PasswordHashers.BCRYPT.hash("123", 12));
        MemberEntity m2 = memberRepo.create("author", PasswordHashers.BCRYPT.hash("123", 12));

        TauMemberEntity reacter = m1.tauMember();
        TauMemberEntity author = m2.tauMember();

        ChatEntity chat = channelRepo.create("memes", author);
        ChatMessageInputDTO input = new ChatMessageInputDTO()
                .setContent("Hello world")
                .setChat(chat)
                .setMember(m1)
                .setSentDatetimeNow()
                .setRepliedToMessages(new HashSet<>())
                .setSys(true);
        MessageEntity msg = messageRepo.create(chat, input, author);

        ReactionPackageEntity basic = reactionPackageRepo.create("basic", "");
        ReactionTypeEntity like = reactionTypeRepo.create("Like", basic);
        ReactionEntryEntity entry = reactionEntryRepo.create(reacter, msg, like);

        boolean removed = reactionEntryRepo.delete(entry);
        assertTrue(removed);

        boolean removedAgain = reactionEntryRepo.delete(entry);
        assertFalse(removedAgain);
    }

    @Test
    public void testRemoveReactionByCompositeKey() throws Exception {
        MemberEntity m1 = memberRepo.create("maria", PasswordHashers.BCRYPT.hash("123", 12));
        MemberEntity m2 = memberRepo.create("mark", PasswordHashers.BCRYPT.hash("123", 12));

        TauMemberEntity reacter = m1.tauMember();
        TauMemberEntity author = m2.tauMember();

        ChatEntity chat = channelRepo.create("random", author);
        ChatMessageInputDTO input = new ChatMessageInputDTO()
                .setContent("Hello world")
                .setChat(chat)
                .setMember(m1)
                .setSentDatetimeNow()
                .setRepliedToMessages(new HashSet<>())
                .setSys(true);
        MessageEntity msg = messageRepo.create(chat, input, author);

        ReactionPackageEntity basic = reactionPackageRepo.create("basic", "");
        ReactionTypeEntity haha = reactionTypeRepo.create("Haha", basic);
        reactionEntryRepo.create(reacter, msg, haha);

        boolean removed = reactionEntryRepo.delete(msg, reacter, haha);
        assertTrue(removed);

        boolean removedAgain = reactionEntryRepo.delete(msg, reacter, haha);
        assertFalse(removedAgain);
    }

    @Test
    public void testActivateInviteCode() throws Exception {
        MemberEntity sender = memberRepo.create("sender", PasswordHashers.BCRYPT.hash("pass", 12));
        MemberEntity receiver = memberRepo.create("receiver", PasswordHashers.BCRYPT.hash("pass", 12));
        TauMemberEntity s = sender.tauMember();
        TauMemberEntity r = receiver.tauMember();

        ChannelEntity channel = channelRepo.create("private", s);
        InviteCodeEntity invite = inviteCodeRepo.create(channel, r, s, ZonedDateTime.now().plusDays(1));

        assertTrue(inviteCodeRepo.activate(invite));
        assertFalse(inviteCodeRepo.activate(invite));
    }

}
