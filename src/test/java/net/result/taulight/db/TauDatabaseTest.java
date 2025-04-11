package net.result.taulight.db;

import net.result.sandnode.db.*;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.BusyNicknameException;
import net.result.sandnode.security.PasswordHashers;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.exception.AlreadyExistingRecordException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TauDatabaseTest {

    private static final Logger LOGGER = LogManager.getLogger(TauDatabaseTest.class);
    private static TauDatabase database;
    private static MemberEntity member1;
    private static MemberEntity member2;

    @BeforeAll
    public static void setup() throws DatabaseException, BusyNicknameException {
        JPAUtil.buildEntityManagerFactory();

        database = new TauJPADatabase(PasswordHashers.BCRYPT);

        member1 = database.registerMember("user1", "password123");
        member2 = database.registerMember("user2", "password123");
    }

    @AfterAll
    public static void tearDown() {
        JPAUtil.shutdown();
    }

    @Test
    public void registerMember() throws DatabaseException, BusyNicknameException {
        MemberEntity newMember = database.registerMember("testuser123", "securePass!");
        assertNotNull(newMember);
        assertEquals("testuser123", newMember.nickname());

        assertThrows(BusyNicknameException.class, () -> database.registerMember("testuser123", "securePass!"));
    }

    @Test
    public void findMemberByNickname() throws DatabaseException, BusyNicknameException {
        database.registerMember("nicksearch", "pass1234");

        Optional<MemberEntity> found = database.findMemberByNickname("nicksearch");
        assertTrue(found.isPresent());
        assertEquals("nicksearch", found.get().nickname());
    }


    @Test
    public void createDialog() throws DatabaseException, AlreadyExistingRecordException, BusyNicknameException {
        MemberEntity member3 = database.registerMember("user3", "password123");
        MemberEntity member4 = database.registerMember("user4", "password123");

        DialogEntity dialog = database.createDialog(member3, member4);
        assertNotNull(dialog);
        if (member3.equals(dialog.firstMember())) {
            assertEquals(member3, dialog.firstMember());
            assertEquals(member4, dialog.secondMember());
        } else {
            assertEquals(member4, dialog.firstMember());
            assertEquals(member3, dialog.secondMember());
        }
    }

    @Test
    public void findDialog() throws DatabaseException, AlreadyExistingRecordException {
        DialogEntity dialog = database.createDialog(member1, member2);
        Optional<DialogEntity> foundDialog = database.findDialog(member1, member2);
        assertTrue(foundDialog.isPresent());
        assertEquals(dialog.id(), foundDialog.get().id());
    }

    @Test
    public void saveChat() throws DatabaseException, AlreadyExistingRecordException {
        ChannelEntity channel = new ChannelEntity("General Chat", member1);
        database.saveChat(channel);

        Optional<ChatEntity> foundChannel = database.getChat(channel.id());
        assertTrue(foundChannel.isPresent());
        assertEquals("General Chat", ((ChannelEntity) foundChannel.get()).title());
    }

    @Test
    public void getChat() throws DatabaseException, AlreadyExistingRecordException {
        ChannelEntity channel = new ChannelEntity("Test Channel", member1);
        database.saveChat(channel);

        Optional<ChatEntity> foundChannel = database.getChat(channel.id());
        assertTrue(foundChannel.isPresent());
        assertEquals(channel.id(), foundChannel.get().id());
    }

    @Test
    public void saveMessage() throws DatabaseException, AlreadyExistingRecordException {
        ChatEntity chat = new ChannelEntity("Test Channel", member1);
        database.saveChat(chat);

        ChatMessageInputDTO messageInputDTO = new ChatMessageInputDTO()
                .setContent("Hello!")
                .setChat(chat)
                .setMember(member1)
                .setSentDatetimeNow()
                .setReplies(new HashSet<>())
                .setSys(true);
        MessageEntity message = new MessageEntity(chat, messageInputDTO, member1);
        database.saveMessage(message);

        Optional<MessageEntity> foundMessage = database.findMessage(message.id());
        assertTrue(foundMessage.isPresent());
        assertEquals("Hello!", foundMessage.get().content());
    }

    @Test
    public void loadMessages() throws DatabaseException, AlreadyExistingRecordException {
        ChannelEntity channel = new ChannelEntity("Test Channel", member1);
        database.saveChat(channel);

        ChatMessageInputDTO input1 = new ChatMessageInputDTO()
                .setContent("Hello world")
                .setChat(channel)
                .setMember(member1)
                .setSentDatetimeNow()
                .setReplies(new HashSet<>())
                .setSys(true);

        ChatMessageInputDTO input2 = new ChatMessageInputDTO()
                .setContent("Hello world")
                .setChat(channel)
                .setMember(member2)
                .setSentDatetimeNow()
                .setReplies(new HashSet<>())
                .setSys(true);

        MessageEntity message1 = new MessageEntity(channel, input1, member1);
        MessageEntity message2 = new MessageEntity(channel, input2, member2);
        database.saveMessage(message1);
        database.saveMessage(message2);

        List<MessageEntity> messages = database.loadMessages(channel, 0, 10);
        assertNotNull(messages);
        assertEquals(2, messages.size());
    }

    @Test
    public void findMessage() throws DatabaseException, AlreadyExistingRecordException {
        ChannelEntity channel = new ChannelEntity("FindMessageChannel", member1);
        database.saveChat(channel);

        ChatMessageInputDTO input = new ChatMessageInputDTO()
                .setContent("Find me")
                .setChat(channel)
                .setMember(member1)
                .setSentDatetimeNow()
                .setReplies(new HashSet<>())
                .setSys(false);

        MessageEntity message = new MessageEntity(channel, input, member1);
        database.saveMessage(message);

        Optional<MessageEntity> found = database.findMessage(message.id());
        assertTrue(found.isPresent());
        assertEquals("Find me", found.get().content());
    }

    @Test
    public void addMemberToChannel() throws DatabaseException, AlreadyExistingRecordException, BusyNicknameException {
        ChannelEntity channel = new ChannelEntity("Test Channel", member1);
        database.saveChat(channel);

        boolean added = database.addMemberToChannel(channel, member2);
        assertTrue(added);

        MemberEntity member5 = database.registerMember("user5", "p");

        Collection<MemberEntity> members = database.getMembers(channel);
        assertTrue(members.stream().anyMatch(m -> m.id().equals(member2.id())));
        assertFalse(members.stream().anyMatch(m -> m.id().equals(member5.id())));
    }

    @Test
    public void getMessageCount() throws DatabaseException, AlreadyExistingRecordException {
        ChannelEntity channel = new ChannelEntity("Test Channel", member1);
        database.saveChat(channel);

        ChatMessageInputDTO input1 = new ChatMessageInputDTO()
                .setContent("Hello world")
                .setChat(channel)
                .setMember(member1)
                .setSentDatetimeNow()
                .setReplies(new HashSet<>())
                .setSys(true);

        ChatMessageInputDTO input2 = new ChatMessageInputDTO()
                .setContent("Hello world")
                .setChat(channel)
                .setMember(member2)
                .setSentDatetimeNow()
                .setReplies(new HashSet<>())
                .setSys(true);

        MessageEntity message1 = new MessageEntity(channel, input1, member1);
        MessageEntity message2 = new MessageEntity(channel, input2, member2);
        database.saveMessage(message1);
        database.saveMessage(message2);

        long count = database.getMessageCount(channel);
        assertEquals(2, count);
    }

    @Test
    public void leaveFromChannel() throws DatabaseException, AlreadyExistingRecordException {
        ChannelEntity channel = new ChannelEntity("Test Channel", member1);
        database.saveChat(channel);

        database.addMemberToChannel(channel, member2);
        boolean removed = database.leaveFromChannel(channel, member2);
        assertTrue(removed);

        Collection<MemberEntity> members = database.getMembers(channel);
        assertFalse(members.stream().anyMatch(m -> m.id().equals(member2.id())));
    }

    @Test
    public void saveInviteCode() throws DatabaseException, AlreadyExistingRecordException {

        ChannelEntity channel = new ChannelEntity("Test Channel", member1);
        database.saveChat(channel);

        ZonedDateTime expiresDate = ZonedDateTime.now().plusDays(1);
        InviteCodeEntity inviteCode = new InviteCodeEntity(channel, member1, member2, expiresDate);
        database.saveInviteCode(inviteCode);

        String stringCode = inviteCode.code();
        Optional<InviteCodeEntity> foundInviteCode = database.getInviteCode(stringCode);
        assertTrue(foundInviteCode.isPresent());
        assertEquals(stringCode, foundInviteCode.get().code());
    }

    @Test
    public void getInviteCode() throws DatabaseException, AlreadyExistingRecordException {
        ChannelEntity channel = new ChannelEntity("InviteChannel", member1);
        database.saveChat(channel);

        ZonedDateTime expiration = ZonedDateTime.now().plusDays(1);
        InviteCodeEntity invite = new InviteCodeEntity(channel, member1, member2, expiration);
        database.saveInviteCode(invite);

        Optional<InviteCodeEntity> result = database.getInviteCode(invite.code());
        assertTrue(result.isPresent());
        assertEquals(invite.code(), result.get().code());
    }

    @Test
    public void activateInviteCode() throws DatabaseException, AlreadyExistingRecordException {
        ChannelEntity channel = new ChannelEntity("Test Channel", member1);
        database.saveChat(channel);

        ZonedDateTime expiresDate = ZonedDateTime.now().plusDays(1);
        InviteCodeEntity inviteCode = new InviteCodeEntity(channel, member1, member2, expiresDate);
        database.saveInviteCode(inviteCode);

        boolean activated = database.activateInviteCode(inviteCode);
        assertTrue(activated);
    }

    @Test
    public void saveReactionType() throws DatabaseException {
        ReactionTypeEntity reactionType = new ReactionTypeEntity("thumbs_up", "emoji_package");
        database.saveReactionType(reactionType);
        assertNotNull(JPAUtil.getEntityManager().find(ReactionTypeEntity.class, reactionType.id()));
    }

    @Test
    public void saveReactionEntry() throws DatabaseException, AlreadyExistingRecordException {
        ReactionTypeEntity reactionType = new ReactionTypeEntity("like", "test");
        database.saveReactionType(reactionType);

        ChannelEntity channel = new ChannelEntity("Test", member1);
        database.saveChat(channel);

        ChatMessageInputDTO input = new ChatMessageInputDTO()
                .setContent("Hello world")
                .setChat(channel)
                .setMember(member1)
                .setSentDatetimeNow()
                .setReplies(new HashSet<>())
                .setSys(true);

        MessageEntity message = new MessageEntity(channel, input, member1);
        database.saveMessage(message);

        ReactionEntryEntity reactionEntry = new ReactionEntryEntity(member1, message, reactionType);
        database.saveReactionEntry(reactionEntry);

        assertEquals("Test", ((ChannelEntity) reactionEntry.message().chat()).title());
    }

    @Test
    public void removeReactionEntry() throws DatabaseException, AlreadyExistingRecordException {
        ReactionTypeEntity reactionType = new ReactionTypeEntity("fire", "test");
        database.saveReactionType(reactionType);

        ChannelEntity channel = new ChannelEntity("Test", member1);
        database.saveChat(channel);

        ChatMessageInputDTO input = new ChatMessageInputDTO()
                .setContent("Hello world")
                .setChat(channel)
                .setMember(member1)
                .setSentDatetimeNow()
                .setReplies(new HashSet<>())
                .setSys(true);

        MessageEntity message = new MessageEntity(channel, input, member1);
        database.saveMessage(message);

        ReactionEntryEntity reactionEntry = new ReactionEntryEntity(member1, message, reactionType);
        database.saveReactionEntry(reactionEntry);

        LOGGER.debug(JPAUtil.getEntityManager().find(ReactionEntryEntity.class, reactionEntry.id()));

        boolean removed = database.removeReactionEntry(reactionEntry);
        assertTrue(removed);
    }

    @Test
    public void getReactionTypesByPackage() throws DatabaseException {
        ReactionTypeEntity rt1 = new ReactionTypeEntity("smile", "funny");
        ReactionTypeEntity rt2 = new ReactionTypeEntity("sad", "funny");
        ReactionTypeEntity rt3 = new ReactionTypeEntity("angry", "angry_pack");

        database.saveReactionType(rt1);
        database.saveReactionType(rt2);
        database.saveReactionType(rt3);

        List<ReactionTypeEntity> funnyReactions = database.getReactionTypesByPackage("funny");
        assertEquals(2, funnyReactions.size());
    }

    @Test
    public void getMembers() throws DatabaseException, AlreadyExistingRecordException, BusyNicknameException {
        ChannelEntity channel = new ChannelEntity("GetMembersChannel", member1);
        database.saveChat(channel);
        database.addMemberToChannel(channel, member2);
        MemberEntity member6 = database.registerMember("user6", "p");

        Collection<MemberEntity> members = database.getMembers(channel);
        assertTrue(members.stream().anyMatch(m -> m.id().equals(member2.id())));
        assertFalse(members.stream().anyMatch(m -> m.id().equals(member6.id())));
    }

    @Test
    public void testRemoveReactionEntry() throws DatabaseException {
        ReactionEntryEntity fakeEntry = new ReactionEntryEntity();
        boolean result = database.removeReactionEntry(fakeEntry);
        assertFalse(result);
    }

}