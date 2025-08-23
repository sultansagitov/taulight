package net.result.taulight.util;

import net.result.sandnode.GlobalTestState;
import net.result.sandnode.entity.MemberEntity;
import net.result.sandnode.repository.MemberRepository;
import net.result.sandnode.util.Container;
import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.db.SimpleJPAUtil;
import net.result.taulight.entity.ChatEntity;
import net.result.taulight.entity.DialogEntity;
import net.result.taulight.entity.GroupEntity;
import net.result.taulight.entity.TauMemberEntity;
import net.result.taulight.repository.DialogRepository;
import net.result.taulight.repository.GroupRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ChatUtilTest {
    private static ChatUtil chatUtil;

    private static MemberEntity member1;
    private static MemberEntity member2;
    private static MemberEntity member3;

    private static GroupEntity group;
    private static DialogEntity dialog;
    private static DialogEntity monolog;

    @BeforeAll
    static void setUp() {
        Container container = GlobalTestState.container;
        MemberRepository memberRepo = container.get(MemberRepository.class);
        GroupRepository groupRepo = container.get(GroupRepository.class);
        DialogRepository dialogRepo = container.get(DialogRepository.class);

        chatUtil = container.get(ChatUtil.class);
        chatUtil = container.get(ChatUtil.class);
        JPAUtil jpaUtil = container.get(SimpleJPAUtil.class);

        member1 = memberRepo.create("member1_chat_util", "hash");
        member2 = memberRepo.create("member2_chat_util", "hash");
        member3 = memberRepo.create("member3_chat_util", "hash");

        GroupEntity g = groupRepo.create("new group", member1.getTauMember());
        groupRepo.addMember(g, member1.getTauMember());
        groupRepo.addMember(g, member2.getTauMember());
        group = jpaUtil.refresh(g);

        dialog = dialogRepo.create(member1.getTauMember(), member2.getTauMember());
        monolog = dialogRepo.create(member3.getTauMember(), member3.getTauMember());
    }

    @Test
    void testGetChatFromGroupRepo() {
        Optional<ChatEntity> result = chatUtil.getChat(group.id());

        boolean present = result.isPresent();
        ChatEntity chat = result.orElse(null);

        assertTrue(present, "Expected chat to be found for group ID");
        assertEquals(group, chat, "Expected returned chat to match the group");
    }


    @Test
    void testGetChatFromDialogRepo() {
        Optional<ChatEntity> result = chatUtil.getChat(dialog.id());

        boolean present = result.isPresent();
        ChatEntity value = result.orElse(null);

        assertTrue(present, "Expected chat to be found for dialog ID");
        assertEquals(dialog, value, "Expected returned chat to match the dialog");
    }

    @Test
    void testGetChatReturnsEmpty() {
        UUID unknownId = UUID.randomUUID();
        Optional<ChatEntity> result = chatUtil.getChat(unknownId);

        boolean present = result.isPresent();

        assertFalse(present, "Expected no chat to be found for random UUID");
    }

    @Test
    void testGetMembersFromGroup() {
        Collection<TauMemberEntity> members = chatUtil.getMembers(group);

        int count = members.size();
        boolean containsExpected = members.containsAll(List.of(member1.getTauMember(), member2.getTauMember()));

        assertEquals(2, count, "Group should contain exactly 2 members");
        assertTrue(containsExpected, "Group should contain member1 and member2");
    }

    @Test
    void testGetMembersFromDialogTwoDifferentMembers() {
        Collection<TauMemberEntity> members = chatUtil.getMembers(dialog);

        int count = members.size();
        boolean containsExpected = members.containsAll(List.of(member1.getTauMember(), member2.getTauMember()));

        assertEquals(2, count, "Dialog should contain exactly 2 members");
        assertTrue(containsExpected, "Dialog should contain both member1 and member2");
    }

    @Test
    void testGetMembersFromDialogSameMember() {
        Collection<TauMemberEntity> members = chatUtil.getMembers(monolog);

        int count = members.size();
        boolean containsExpected = members.contains(member3.getTauMember());

        assertEquals(1, count, "Monolog should contain exactly 1 member");
        assertTrue(containsExpected, "Monolog should contain member3 only");
    }


    @Test
    void testContainsInGroup() {
        boolean contains1 = chatUtil.contains(group, member1.getTauMember());

        assertTrue(contains1);
    }

    @Test
    void testContainsInDialog() {
        boolean contains1 = chatUtil.contains(dialog, member1.getTauMember());
        boolean contains2 = chatUtil.contains(dialog, member2.getTauMember());
        boolean contains3 = chatUtil.contains(dialog, member3.getTauMember());

        assertTrue(contains1);
        assertTrue(contains2);
        assertFalse(contains3);
    }
}
