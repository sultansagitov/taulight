package net.result.taulight.db;

import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.BusyNicknameException;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.security.PasswordHashers;
import net.result.taulight.dto.ChatMessageInputDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class MessagesTest {
    private static TauDatabase database;
    private static TauMemberEntity member1;
    private static TauMemberEntity member2;

    @BeforeAll
    public static void setup() throws DatabaseException, BusyNicknameException {
        JPAUtil.buildEntityManagerFactory();
        database = new TauJPADatabase(PasswordHashers.BCRYPT);

        member1 = database.registerMember("user1_messages", "password123").tauMember();
        member2 = database.registerMember("user2_messages", "password123").tauMember();

        assertNotNull(member1.id());
        assertNotNull(member2.id());
    }

    @Test
    public void createMessage() throws DatabaseException, NotFoundException {
        ChatEntity chat = database.createChannel("Test Channel", member1);

        ChatMessageInputDTO messageInputDTO = new ChatMessageInputDTO()
                .setContent("Hello!")
                .setChat(chat)
                .setMember(member1.member())
                .setSentDatetimeNow()
                .setRepliedToMessages(new HashSet<>())
                .setSys(true);
        MessageEntity message = database.createMessage(chat, messageInputDTO, member1);

        Optional<MessageEntity> foundMessage = database.findMessage(message.id());
        assertTrue(foundMessage.isPresent());
        assertEquals("Hello!", foundMessage.get().content());

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
    public void loadMessages() throws DatabaseException, NotFoundException {
        ChannelEntity channel = database.createChannel("Test Channel", member1);

        ChatMessageInputDTO input1 = new ChatMessageInputDTO()
                .setContent("Hello world")
                .setChat(channel)
                .setMember(member1.member())
                .setSentDatetimeNow()
                .setRepliedToMessages(new HashSet<>())
                .setSys(true);

        ChatMessageInputDTO input2 = new ChatMessageInputDTO()
                .setContent("Hello world")
                .setChat(channel)
                .setMember(member2.member())
                .setSentDatetimeNow()
                .setRepliedToMessages(new HashSet<>())
                .setSys(true);

        MessageEntity message1 = database.createMessage(channel, input1, member1);
        MessageEntity message2 = database.createMessage(channel, input2, member2);

        List<MessageEntity> messages = database.loadMessages(channel, 0, 10);
        assertNotNull(messages);
        assertEquals(2, messages.size());

        // Additional assertions
        assertTrue(messages.containsAll(List.of(message1, message2)), "Retrieved messages should contain both created messages");

        // Test pagination
        List<MessageEntity> firstPage = database.loadMessages(channel, 0, 1);
        assertEquals(1, firstPage.size(), "Should retrieve only one message when limit is 1");

        List<MessageEntity> secondPage = database.loadMessages(channel, 1, 1);
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
    public void findMessage() throws DatabaseException, NotFoundException {
        ChannelEntity channel = database.createChannel("FindMessageChannel", member1);

        ChatMessageInputDTO input = new ChatMessageInputDTO()
                .setContent("Find me")
                .setChat(channel)
                .setMember(member1.member())
                .setSentDatetimeNow()
                .setRepliedToMessages(new HashSet<>())
                .setSys(false);

        MessageEntity message = database.createMessage(channel, input, member1);

        Optional<MessageEntity> found = database.findMessage(message.id());
        assertTrue(found.isPresent());
        assertEquals("Find me", found.get().content());

        // Additional assertions
        assertEquals(message.id(), found.get().id(), "IDs should match");
        assertEquals(message.member(), found.get().member(), "Members should match");
        assertEquals(message.chat(), found.get().chat(), "Chats should match");
        assertFalse(found.get().sys(), "Message should not be a system message");

        // Test with non-existent message ID
        UUID nonExistentID = UUID.randomUUID();
        Optional<MessageEntity> nonExistentMessage = database.findMessage(nonExistentID);
        assertFalse(nonExistentMessage.isPresent(), "Should not find non-existent message");
    }

    @Test
    public void getMessageCount() throws DatabaseException, NotFoundException {
        ChannelEntity channel = database.createChannel("Test Channel", member1);

        ChatMessageInputDTO input1 = new ChatMessageInputDTO()
                .setContent("Hello world")
                .setChat(channel)
                .setMember(member1.member())
                .setSentDatetimeNow()
                .setRepliedToMessages(new HashSet<>())
                .setSys(true);

        ChatMessageInputDTO input2 = new ChatMessageInputDTO()
                .setContent("Hello world")
                .setChat(channel)
                .setMember(member2.member())
                .setSentDatetimeNow()
                .setRepliedToMessages(new HashSet<>())
                .setSys(true);

        database.createMessage(channel, input1, member1);
        database.createMessage(channel, input2, member2);

        long count = database.getMessageCount(channel);
        assertEquals(2, count);
        assertEquals(channel.messages().size(), count);

        // Additional assertions
        // Add one more message and verify count increases
        ChatMessageInputDTO input3 = new ChatMessageInputDTO()
                .setContent("Third message")
                .setChat(channel)
                .setMember(member1.member())
                .setSentDatetimeNow()
                .setRepliedToMessages(new HashSet<>())
                .setSys(false);

        database.createMessage(channel, input3, member1);
        long newCount = database.getMessageCount(channel);
        assertEquals(3, newCount, "Count should increase to 3 after adding a third message");

        // Test with a new empty channel
        ChannelEntity emptyChannel = database.createChannel("Empty Channel", member1);
        long emptyCount = database.getMessageCount(emptyChannel);
        assertEquals(0, emptyCount, "Empty channel should have zero messages");
    }

}
