package net.result.taulight.db;

import net.result.sandnode.GlobalTestState;
import net.result.sandnode.db.MemberRepository;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.SandnodeException;
import net.result.sandnode.exception.error.BusyNicknameException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.JPAUtil;
import net.result.sandnode.util.SimpleJPAUtil;
import net.result.taulight.dto.ChatMessageInputDTO;
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
    public static void setup() throws DatabaseException, BusyNicknameException {
        Container container = GlobalTestState.container;
        jpaUtil = container.get(SimpleJPAUtil.class);

        MemberRepository memberRepo = container.get(MemberRepository.class);
        groupRepo = container.get(GroupRepository.class);
        messageRepo = container.get(MessageRepository.class);

        member1 = memberRepo.create("user1_messages", "hash").tauMember();
        member2 = memberRepo.create("user2_messages", "hash").tauMember();

        assertNotNull(member1.id());
        assertNotNull(member2.id());
    }

    @Test
    public void createMessage() throws SandnodeException {
        ChatEntity chat = groupRepo.create("Test Group", member1);

        ChatMessageInputDTO messageInputDTO = new ChatMessageInputDTO()
                .setContent("Hello!")
                .setChat(chat)
                .setMember(member1.member())
                .setSentDatetimeNow()
                .setRepliedToMessages(new HashSet<>())
                .setSys(true);
        MessageEntity message = messageRepo.create(chat, messageInputDTO, member1);

        MessageEntity foundMessage = jpaUtil.refresh(message);
        assertEquals("Hello!", foundMessage.content());

        // Additional assertions
        assertNotNull(message.id(), "Message ID should not be null");
        assertEquals(member1, message.member(), "Message member should be the creator");
        assertEquals(chat, message.chat(), "Message chat should be the specified chat");
        assertTrue(message.sys(), "Message should be marked as system message");
        assertNotNull(message.sentDatetime(), "Sent datetime should not be null");
        assertTrue(chat.messages().contains(message), "Chat should contain the new message");
        assertEquals(0, message.repliedToMessages().size(), "No replied-to messages should be present");
        assertEquals(0, message.reactionEntries().size(), "No reactions should be present on new message");
    }

    @Test
    public void loadMessages() throws DatabaseException, UnauthorizedException {
        GroupEntity group = groupRepo.create("Test Group", member1);

        ChatMessageInputDTO input1 = new ChatMessageInputDTO()
                .setContent("Hello world")
                .setChat(group)
                .setMember(member1.member())
                .setSentDatetimeNow()
                .setRepliedToMessages(new HashSet<>())
                .setSys(true);

        ChatMessageInputDTO input2 = new ChatMessageInputDTO()
                .setContent("Hello world")
                .setChat(group)
                .setMember(member2.member())
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
    public void findMessage() throws SandnodeException {
        GroupEntity group = groupRepo.create("FindMessageGroup", member1);

        ChatMessageInputDTO input = new ChatMessageInputDTO()
                .setContent("Find me")
                .setChat(group)
                .setMember(member1.member())
                .setSentDatetimeNow()
                .setRepliedToMessages(new HashSet<>())
                .setSys(false);

        MessageEntity message = messageRepo.create(group, input, member1);

        MessageEntity found = jpaUtil.refresh(message);
        assertEquals("Find me", found.content());

        // Additional assertions
        assertEquals(message.id(), found.id(), "IDs should match");
        assertEquals(message.member(), found.member(), "Members should match");
        assertEquals(message.chat(), found.chat(), "Chats should match");
        assertFalse(found.sys(), "Message should not be a system message");

        // Test with non-existent message ID
        UUID nonExistentID = UUID.randomUUID();
        Optional<MessageEntity> nonExistentMessage = jpaUtil.find(MessageEntity.class, nonExistentID);
        assertFalse(nonExistentMessage.isPresent(), "Should not find non-existent message");
    }

    @Test
    public void getMessageCount() throws SandnodeException {
        GroupEntity group = groupRepo.create("Test Group", member1);

        ChatMessageInputDTO input1 = new ChatMessageInputDTO()
                .setContent("Hello world")
                .setChat(group)
                .setMember(member1.member())
                .setSentDatetimeNow()
                .setRepliedToMessages(new HashSet<>())
                .setSys(true);

        ChatMessageInputDTO input2 = new ChatMessageInputDTO()
                .setContent("Hello world")
                .setChat(group)
                .setMember(member2.member())
                .setSentDatetimeNow()
                .setRepliedToMessages(new HashSet<>())
                .setSys(true);

        messageRepo.create(group, input1, member1);
        messageRepo.create(group, input2, member2);

        long count = messageRepo.countMessagesByChat(group);
        assertEquals(2, count);
        assertEquals(group.messages().size(), count);

        // Additional assertions
        // Add one more message and verify count increases
        ChatMessageInputDTO input3 = new ChatMessageInputDTO()
                .setContent("Third message")
                .setChat(group)
                .setMember(member1.member())
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
