package net.result.taulight.repository;

import net.result.sandnode.GlobalTestState;
import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.repository.MemberRepository;
import net.result.sandnode.util.Container;
import net.result.taulight.db.TauMemberCreationListener;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.entity.ChatEntity;
import net.result.taulight.entity.GroupEntity;
import net.result.taulight.entity.MessageEntity;
import net.result.taulight.entity.TauMemberEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class MessagesTest {
    private static JPAUtil jpaUtil;
    private static TauMemberEntity member1;
    private static TauMemberEntity member2;
    private static GroupRepository groupRepo;
    private static MessageRepository messageRepo;

    @BeforeAll
    public static void setup() {
        Container container = GlobalTestState.container;

        container.addInstanceItem(TauMemberCreationListener.class);

        jpaUtil = container.get(JPAUtil.class);

        MemberRepository memberRepo = container.get(MemberRepository.class);
        TauMemberRepository tauMemberRepo = container.get(TauMemberRepository.class);
        groupRepo = container.get(GroupRepository.class);
        messageRepo = container.get(MessageRepository.class);

        member1 = tauMemberRepo.findByMember(memberRepo.create("user1_messages", "hash"));
        member2 = tauMemberRepo.findByMember(memberRepo.create("user2_messages", "hash"));

        assertNotNull(member1.id());
        assertNotNull(member2.id());
    }

    @Test
    public void createMessage() {
        ChatEntity chat = groupRepo.create("Test Group", member1);

        ChatMessageInputDTO messageInputDTO = new ChatMessageInputDTO()
                .setContent("Hello!")
                .setChatID(chat.id())
                .setNickname(member1.getMember().getNickname())
                .setSentDatetimeNow()
                .setRepliedToMessages(new HashSet<>())
                .setSys(true);
        MessageEntity message = messageRepo.create(chat, messageInputDTO, member1);

        MessageEntity foundMessage = jpaUtil.refresh(message);
        assertEquals("Hello!", foundMessage.getContent());

        // Additional assertions
        assertNotNull(message.id(), "Message ID should not be null");
        assertEquals(member1, message.getMember(), "Message member should be the creator");
        assertEquals(chat, message.getChat(), "Message chat should be the specified chat");
        assertTrue(message.isSys(), "Message should be marked as system message");
        assertNotNull(message.getSentDatetime(), "Sent datetime should not be null");
        assertTrue(chat.getMessages().contains(message), "Chat should contain the new message");
        assertEquals(0, message.getRepliedToMessages().size(), "No replied-to messages should be present");
        assertEquals(0, message.getReactionEntries().size(), "No reactions should be present on new message");
    }

    @Test
    public void loadMessages() {
        GroupEntity group = groupRepo.create("Test Group", member1);

        ChatMessageInputDTO input1 = new ChatMessageInputDTO()
                .setContent("Hello world")
                .setChatID(group.id())
                .setNickname(member1.getMember().getNickname())
                .setSentDatetimeNow()
                .setRepliedToMessages(new HashSet<>())
                .setSys(true);

        ChatMessageInputDTO input2 = new ChatMessageInputDTO()
                .setContent("Hello world")
                .setChatID(group.id())
                .setNickname(member2.getMember().getNickname())
                .setSentDatetimeNow()
                .setRepliedToMessages(new HashSet<>())
                .setSys(true);

        MessageEntity message1 = messageRepo.create(group, input1, member1);
        MessageEntity message2 = messageRepo.create(group, input2, member2);

        List<MessageEntity> messages = messageRepo.findMessagesByChat(group, 0, 10);
        assertNotNull(messages);
        assertEquals(2, messages.size());

        // Additional assertions
        assertTrue(messages.containsAll(List.of(message1, message2)),
                "Retrieved messages should contain both created messages");

        // Test pagination
        List<MessageEntity> firstPage = messageRepo.findMessagesByChat(group, 0, 1);
        assertEquals(1, firstPage.size(), "Should retrieve only one message when limit is 1");

        List<MessageEntity> secondPage = messageRepo.findMessagesByChat(group, 1, 1);
        assertEquals(1, secondPage.size(), "Should retrieve one message from second page");

        // The combination of both pages should contain all messages
        Set<MessageEntity> allMessages = new HashSet<>();
        allMessages.addAll(firstPage);
        allMessages.addAll(secondPage);
        assertEquals(1, firstPage.size(), "first page should contain one message");
        assertEquals(1, secondPage.size(), "second page should contain one message");
        assertNotEquals(firstPage.get(0), secondPage.get(0));
        assertEquals(2, allMessages.size(), "Combined pages should contain all messages");
    }

    @Test
    public void findMessage() {
        GroupEntity group = groupRepo.create("FindMessageGroup", member1);

        ChatMessageInputDTO input = new ChatMessageInputDTO()
                .setContent("Find me")
                .setChatID(group.id())
                .setNickname(member1.getMember().getNickname())
                .setSentDatetimeNow()
                .setRepliedToMessages(new HashSet<>())
                .setSys(false);

        MessageEntity message = messageRepo.create(group, input, member1);

        MessageEntity found = jpaUtil.refresh(message);
        assertEquals("Find me", found.getContent());

        // Additional assertions
        assertEquals(message.id(), found.id(), "IDs should match");
        assertEquals(message.getMember(), found.getMember(), "Members should match");
        assertEquals(message.getChat(), found.getChat(), "Chats should match");
        assertFalse(found.isSys(), "Message should not be a system message");

        // Test with non-existent message ID
        UUID nonExistentID = UUID.randomUUID();
        Optional<MessageEntity> nonExistentMessage = jpaUtil.find(MessageEntity.class, nonExistentID);
        assertFalse(nonExistentMessage.isPresent(), "Should not find non-existent message");
    }

    @Test
    public void getMessageCount() {
        GroupEntity group = groupRepo.create("Test Group", member1);

        ChatMessageInputDTO input1 = new ChatMessageInputDTO()
                .setContent("Hello world")
                .setChatID(group.id())
                .setNickname(member1.getMember().getNickname())
                .setSentDatetimeNow()
                .setRepliedToMessages(new HashSet<>())
                .setSys(true);

        ChatMessageInputDTO input2 = new ChatMessageInputDTO()
                .setContent("Hello world")
                .setChatID(group.id())
                .setNickname(member2.getMember().getNickname())
                .setSentDatetimeNow()
                .setRepliedToMessages(new HashSet<>())
                .setSys(true);

        messageRepo.create(group, input1, member1);
        messageRepo.create(group, input2, member2);

        long count = messageRepo.countMessagesByChat(group);
        assertEquals(2, count);
        assertEquals(group.getMessages().size(), count);

        // Additional assertions
        // Add one more message and verify count increases
        ChatMessageInputDTO input3 = new ChatMessageInputDTO()
                .setContent("Third message")
                .setChatID(group.id())
                .setNickname(member1.getMember().getNickname())
                .setSentDatetimeNow()
                .setRepliedToMessages(new HashSet<>())
                .setSys(false);

        messageRepo.create(group, input3, member1);
        long newCount = messageRepo.countMessagesByChat(group);
        assertEquals(3, newCount, "Count should increase to 3 after adding a third message");

        // Test with a new empty group
        GroupEntity emptyGroup = groupRepo.create("Empty Group", member1);
        long emptyCount = messageRepo.countMessagesByChat(emptyGroup);
        assertEquals(0, emptyCount, "Empty group should have zero messages");
    }
}
