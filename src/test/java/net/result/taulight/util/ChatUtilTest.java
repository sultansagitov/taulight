package net.result.taulight.util;

import net.result.sandnode.GlobalTestState;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.db.MemberRepository;
import net.result.sandnode.exception.AlreadyExistingRecordException;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.BusyNicknameException;
import net.result.sandnode.util.Container;
import net.result.taulight.db.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ChatUtilTest {

    private static ChatUtil chatUtil;

    private static MemberEntity member1;
    private static MemberEntity member2;
    private static MemberEntity member3;

    private static ChannelEntity channel;
    private static DialogEntity dialog;
    private static DialogEntity monolog;

    @BeforeAll
    static void setUp() throws BusyNicknameException, DatabaseException, AlreadyExistingRecordException {
        Container container = GlobalTestState.container;
        MemberRepository memberRepo = container.get(MemberRepository.class);
        ChannelRepository channelRepo = container.get(ChannelRepository.class);
        DialogRepository dialogRepo = container.get(DialogRepository.class);

        chatUtil = container.get(ChatUtil.class);

        member1 = memberRepo.create("member1_chat_util", "hash");
        member2 = memberRepo.create("member2_chat_util", "hash");
        member3 = memberRepo.create("member3_chat_util", "hash");

        channel = channelRepo.create("new channel", member1.tauMember());
        channelRepo.addMember(channel, member1.tauMember());
        channelRepo.addMember(channel, member2.tauMember());

        dialog = dialogRepo.create(member1.tauMember(), member2.tauMember());
        monolog = dialogRepo.create(member3.tauMember(), member3.tauMember());

    }

    @Test
    void testGetChatFromChannelRepo() throws DatabaseException {

        Optional<ChatEntity> result = chatUtil.getChat(channel.id());

        assertTrue(result.isPresent());
        assertEquals(channel, result.get());
    }

    @Test
    void testGetChatFromDialogRepo() throws DatabaseException {
        Optional<ChatEntity> result = chatUtil.getChat(dialog.id());

        assertTrue(result.isPresent());
        assertEquals(dialog, result.get());
    }

    @Test
    void testGetChatReturnsEmpty() throws DatabaseException {
        Optional<ChatEntity> result = chatUtil.getChat(UUID.randomUUID());

        assertFalse(result.isPresent());
    }

    @Test
    void testGetMembersFromChannel() {
        Collection<TauMemberEntity> members = chatUtil.getMembers(channel);

        assertEquals(2, members.size());
        assertTrue(members.containsAll(List.of(member1.tauMember(), member2.tauMember())));
    }

    @Test
    void testGetMembersFromDialogTwoDifferentMembers() {
        Collection<TauMemberEntity> members = chatUtil.getMembers(dialog);

        assertEquals(2, members.size());
        assertTrue(members.containsAll(List.of(member1.tauMember(), member2.tauMember())));
    }

    @Test
    void testGetMembersFromDialogSameMember() {
        Collection<TauMemberEntity> members = chatUtil.getMembers(monolog);

        assertEquals(1, members.size());
        assertTrue(members.contains(member3.tauMember()));
    }

    @Test
    void testContainsInChannel() throws DatabaseException {
        boolean result = chatUtil.contains(channel, member1.tauMember());

        assertTrue(result);
    }

    @Test
    void testContainsInDialog() throws DatabaseException {
        assertTrue(chatUtil.contains(dialog, member1.tauMember()));
        assertTrue(chatUtil.contains(dialog, member2.tauMember()));
        assertFalse(chatUtil.contains(dialog, member3.tauMember()));
    }
}
