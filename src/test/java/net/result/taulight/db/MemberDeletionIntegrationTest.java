package net.result.taulight.db;

import net.result.sandnode.GlobalTestState;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.db.MemberRepository;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.JPAUtil;
import net.result.taulight.dto.ChatMessageInputDTO;
import org.junit.jupiter.api.*;

import java.time.ZonedDateTime;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MemberDeletionIntegrationTest {
    private static JPAUtil jpaUtil;
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
        Container container = GlobalTestState.container;
        jpaUtil = container.get(JPAUtil.class);
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
        MemberEntity m1 = memberRepo.create("alice", "hash");
        MemberEntity m2 = memberRepo.create("bob", "hash");

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
        MemberEntity member = memberRepo.create("charlie", "hash");
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

        member = jpaUtil.refresh(member);

        boolean deleted = memberRepo.delete(member);

        assertTrue(deleted);
        assertEquals(1, messageRepo.countMessagesByChat(chat));
    }

    @Test
    public void testDeleteMemberAndCheckReactionCleanup() throws Exception {
        MemberEntity m1 = memberRepo.create("eva", "hash");
        MemberEntity m2 = memberRepo.create("oliver", "hash");
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
        MemberEntity owner = memberRepo.create("sam", "hash");
        MemberEntity invited = memberRepo.create("jack", "hash");
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
