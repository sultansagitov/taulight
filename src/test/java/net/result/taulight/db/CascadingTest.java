package net.result.taulight.db;

import net.result.sandnode.GlobalTestState;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.db.MemberRepository;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.JPAUtil;
import net.result.taulight.dto.ChatMessageInputDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

public class CascadingTest {
    private static JPAUtil jpaUtil;
    private static MemberRepository memberRepo;
    private static GroupRepository groupRepo;
    private static MessageRepository messageRepo;
    private static InviteCodeRepository inviteCodeRepo;
    private static ReactionPackageRepository reactionPackageRepo;
    private static ReactionTypeRepository reactionTypeRepo;
    private static ReactionEntryRepository reactionEntryRepo;

    @BeforeAll
    public static void setup() {
        Container container = GlobalTestState.container;
        jpaUtil = container.get(JPAUtil.class);
        memberRepo = container.get(MemberRepository.class);
        groupRepo = container.get(GroupRepository.class);
        messageRepo = container.get(MessageRepository.class);
        inviteCodeRepo = container.get(InviteCodeRepository.class);
        reactionPackageRepo = container.get(ReactionPackageRepository.class);
        reactionTypeRepo = container.get(ReactionTypeRepository.class);
        reactionEntryRepo = container.get(ReactionEntryRepository.class);
    }

    @Test
    public void testLeaveFromGroup() throws Exception {
        MemberEntity m1 = memberRepo.create("new_user", "hash");
        TauMemberEntity tau = m1.tauMember();

        GroupEntity group = groupRepo.create("news", tau);

        boolean added = groupRepo.addMember(group, tau);
        boolean left = groupRepo.removeMember(group, tau);
        group = jpaUtil.refresh(group);
        boolean leftAgain = groupRepo.removeMember(group, tau);

        assertFalse(added);
        assertTrue(left);
        assertFalse(leftAgain);
    }

    @Test
    public void testRemoveReactionByObject() throws Exception {
        MemberEntity m1 = memberRepo.create("reacter", "hash");
        MemberEntity m2 = memberRepo.create("author", "hash");

        TauMemberEntity reacter = m1.tauMember();
        TauMemberEntity author = m2.tauMember();

        ChatEntity chat = groupRepo.create("memes", author);
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
        boolean removedAgain = reactionEntryRepo.delete(entry);

        assertTrue(removed);
        assertFalse(removedAgain);
    }

    @Test
    public void testRemoveReactionByCompositeKey() throws Exception {
        MemberEntity m1 = memberRepo.create("maria", "hash");
        MemberEntity m2 = memberRepo.create("mark", "hash");

        TauMemberEntity reacter = m1.tauMember();
        TauMemberEntity author = m2.tauMember();

        ChatEntity chat = groupRepo.create("random", author);
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
        boolean removedAgain = reactionEntryRepo.delete(msg, reacter, haha);

        assertTrue(removed);
        assertFalse(removedAgain);
    }

    @Test
    public void testActivateInviteCode() throws Exception {
        MemberEntity sender = memberRepo.create("sender_cascading", "hash");
        MemberEntity receiver = memberRepo.create("receiver_cascading", "hash");
        TauMemberEntity s = sender.tauMember();
        TauMemberEntity r = receiver.tauMember();

        GroupEntity group = groupRepo.create("private", s);
        InviteCodeEntity invite = inviteCodeRepo.create(group, r, s, ZonedDateTime.now().plusDays(1));

        inviteCodeRepo.activate(invite);
        inviteCodeRepo.activate(invite);
    }
}
