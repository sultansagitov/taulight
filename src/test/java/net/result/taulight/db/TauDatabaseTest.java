package net.result.taulight.db;

import net.result.sandnode.db.*;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.BusyNicknameException;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.security.PasswordHashers;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.exception.AlreadyExistingRecordException;
import org.junit.jupiter.api.*;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TauDatabaseTest {

    private static TauDatabase database;
    private static TauMemberEntity member1;
    private static TauMemberEntity member2;
    private static TauMemberEntity member3;
    private static TauMemberEntity member4;
    private static TauMemberEntity member5;
    private static TauMemberEntity member6;

    @BeforeAll
    public static void setup() throws DatabaseException, BusyNicknameException {
        JPAUtil.buildEntityManagerFactory();

        database = new TauJPADatabase(PasswordHashers.BCRYPT);

        member1 = database.registerMember("user1", "password123").tauMember();
        member2 = database.registerMember("user2", "password123").tauMember();
        member3 = database.registerMember("user3", "password123").tauMember();
        member4 = database.registerMember("user4", "password123").tauMember();
        member5 = database.registerMember("user5", "password123").tauMember();
        member6 = database.registerMember("user6", "password123").tauMember();
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
    public void createDialog() throws DatabaseException, AlreadyExistingRecordException {
        DialogEntity dialog = database.createDialog(member3, member4);
        assertNotNull(dialog);
        if (member3 == dialog.firstMember()) {
            assertEquals(member3, dialog.firstMember());
            assertEquals(member4, dialog.secondMember());
        } else {
            assertEquals(member4, dialog.firstMember());
            assertEquals(member3, dialog.secondMember());
        }
    }

    @Test
    public void findDialog() throws DatabaseException, AlreadyExistingRecordException {
        database.createDialog(member5, member6);
        Optional<DialogEntity> foundDialog = database.findDialog(member5, member6);
        assertTrue(foundDialog.isPresent());
    }

    @Test
    public void createChannel() throws DatabaseException {
        ChannelEntity channel = database.createChannel("General Chat", member1);

        Optional<ChatEntity> foundChannel = database.getChat(channel.id());
        assertTrue(foundChannel.isPresent());
        assertEquals("General Chat", ((ChannelEntity) foundChannel.get()).title());
    }

    @Test
    public void getChat() throws DatabaseException {
        ChannelEntity channel = database.createChannel("Test Channel", member1);

        Optional<ChatEntity> foundChannel = database.getChat(channel.id());
        assertTrue(foundChannel.isPresent());
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

        database.createMessage(channel, input1, member1);
        database.createMessage(channel, input2, member2);

        List<MessageEntity> messages = database.loadMessages(channel, 0, 10);
        assertNotNull(messages);
        assertEquals(2, messages.size());
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
    }

    @Test
    public void addMemberToChannel() throws DatabaseException {
        ChannelEntity channel = database.createChannel("Test Channel", member1);

        boolean added = database.addMemberToChannel(channel, member2);
        assertTrue(added);

        Collection<TauMemberEntity> members = database.getMembers(channel);
        assertTrue(members.contains(member2));
        assertFalse(members.contains(member5));

        assertTrue(member2.channels().contains(channel));
        assertFalse(member5.channels().contains(channel));
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
    }

    @Test
    public void leaveFromChannel() throws DatabaseException {
        ChannelEntity channel = database.createChannel("Test Channel", member1);

        database.addMemberToChannel(channel, member2);
        boolean removed = database.leaveFromChannel(channel, member2);
        assertTrue(removed);

        Collection<TauMemberEntity> members = database.getMembers(channel);
        assertFalse(members.contains(member2));
    }

    @Test
    public void createInviteCode() throws DatabaseException {

        ChannelEntity channel = database.createChannel("Test Channel", member1);

        ZonedDateTime expiresDate = ZonedDateTime.now().plusDays(1);
        InviteCodeEntity inviteCode = database.createInviteCode(channel, member1, member2, expiresDate);

        String stringCode = inviteCode.code();
        Optional<InviteCodeEntity> foundInviteCode = database.findInviteCode(stringCode);
        assertTrue(foundInviteCode.isPresent());
        assertEquals(stringCode, foundInviteCode.get().code());
    }

    @Test
    public void findInviteCode1() throws DatabaseException {
        ChannelEntity channel = database.createChannel("InviteChannel", member1);

        ZonedDateTime expiration = ZonedDateTime.now().plusDays(1);
        InviteCodeEntity invite = database.createInviteCode(channel, member2, member1, expiration);

        Optional<InviteCodeEntity> result = database.findInviteCode(invite.code());
        assertTrue(result.isPresent());
        assertEquals(invite, result.get());
    }

    @Test
    public void findInviteCode2() throws DatabaseException {
        ChannelEntity channel = database.createChannel("InviteChannel", member3);

        ZonedDateTime expiration = ZonedDateTime.now().plusDays(1);
        database.createInviteCode(channel, member4, member3, expiration);
        database.createInviteCode(channel, member4, member3, expiration);

        Collection<InviteCodeEntity> result = database.findInviteCode(channel, member4);
        assertEquals(2, result.size());
    }

    @Test
    public void activateInviteCode() throws DatabaseException {
        ChannelEntity channel = database.createChannel("Test Channel", member1);

        ZonedDateTime expiresDate = ZonedDateTime.now().plusDays(1);
        InviteCodeEntity inviteCode = database.createInviteCode(channel, member1, member2, expiresDate);

        boolean activated = database.activateInviteCode(inviteCode);
        assertTrue(activated);
    }

    @Test
    public void createReactionPackage() throws DatabaseException {
        ReactionPackageEntity reactionPackage = database.createReactionPackage("funny_emojis", "");
        assertNotNull(reactionPackage);
        assertEquals("funny_emojis", reactionPackage.name());

        ReactionPackageEntity found = JPAUtil.getEntityManager().find(ReactionPackageEntity.class, reactionPackage.id());
        assertNotNull(found);
        assertEquals(reactionPackage.id(), found.id());
    }

    @Test
    public void findReactionPackage() throws DatabaseException {
        database.createReactionPackage("qwe", "rty");
        Optional<ReactionPackageEntity> found = database.findReactionPackage("qwe");
        assertTrue(found.isPresent());
        assertEquals("rty", found.get().description());
    }

    @Test
    public void createReactionType1() throws DatabaseException {
        ReactionPackageEntity reactionPackage = database.createReactionPackage("standard", "");
        ReactionTypeEntity reactionType = database.createReactionType("laugh", reactionPackage);

        assertNotNull(reactionType);
        assertEquals("laugh", reactionType.name());
        assertEquals("standard", reactionType.reactionPackage().name());

        ReactionTypeEntity found = JPAUtil.getEntityManager().find(ReactionTypeEntity.class, reactionType.id());
        assertNotNull(found);
        assertEquals("laugh", found.name());
    }

    @Test
    public void createReactionType2() throws DatabaseException {
        ReactionPackageEntity reactionPackage = database.createReactionPackage("multi_package", "");

        Collection<String> typeNames = List.of("clap", "wow", "heart");

        Collection<ReactionTypeEntity> createdTypes = database.createReactionType(reactionPackage, typeNames);

        assertNotNull(createdTypes);
        assertEquals(3, createdTypes.size());

        for (ReactionTypeEntity type : createdTypes) {
            assertTrue(typeNames.contains(type.name()));
            assertEquals(reactionPackage.id(), type.reactionPackage().id());

            ReactionTypeEntity found = JPAUtil.getEntityManager().find(ReactionTypeEntity.class, type.id());
            assertNotNull(found);
            assertEquals(type, found);
        }
    }

    @Test
    public void createReactionEntry() throws DatabaseException, NotFoundException {
        ReactionPackageEntity testPackage = database.createReactionPackage("test", "");
        ReactionTypeEntity reactionType = database.createReactionType("like", testPackage);

        ChannelEntity channel = database.createChannel("Test", member1);

        ChatMessageInputDTO input = new ChatMessageInputDTO()
                .setContent("Hello world")
                .setChat(channel)
                .setMember(member1.member())
                .setSentDatetimeNow()
                .setRepliedToMessages(new HashSet<>())
                .setSys(true);

        MessageEntity message = database.createMessage(channel, input, member1);

        ReactionEntryEntity reactionEntry = database.createReactionEntry(member1, message, reactionType);

        assertEquals("Test", ((ChannelEntity) reactionEntry.message().chat()).title());
    }

    @Test
    public void removeReactionEntry() throws DatabaseException, NotFoundException {
        ReactionPackageEntity testPackage = database.createReactionPackage("test", "");
        ReactionTypeEntity reactionType = database.createReactionType("fire", testPackage);

        ChannelEntity channel = database.createChannel("Test", member1);

        ChatMessageInputDTO input = new ChatMessageInputDTO()
                .setContent("Hello world")
                .setChat(channel)
                .setMember(member1.member())
                .setSentDatetimeNow()
                .setRepliedToMessages(new HashSet<>())
                .setSys(true);

        MessageEntity message = database.createMessage(channel, input, member1);

        ReactionEntryEntity reactionEntry = database.createReactionEntry(member1, message, reactionType);

        boolean removed = database.removeReactionEntry(reactionEntry);
        assertTrue(removed);
    }

    @Test
    public void getReactionTypesByPackage() throws DatabaseException {
        ReactionPackageEntity funnyPackage = database.createReactionPackage("funny", "");
        ReactionPackageEntity angryPackage = database.createReactionPackage("angry_pack", "");

        database.createReactionType("smile", funnyPackage);
        database.createReactionType("sad", funnyPackage);
        database.createReactionType("angry", angryPackage);

        List<ReactionTypeEntity> funnyReactions = database.getReactionTypesByPackage("funny");
        assertEquals(2, funnyReactions.size());
    }

    @Test
    public void getMembers() throws DatabaseException {
        ChannelEntity channel = database.createChannel("GetMembersChannel", member1);
        database.addMemberToChannel(channel, member2);

        Collection<TauMemberEntity> members = database.getMembers(channel);
        assertTrue(members.contains(member2));
        assertFalse(members.contains(member6));
    }

    @Test
    public void testRemoveReactionEntry() throws DatabaseException {
        ReactionEntryEntity fakeEntry = new ReactionEntryEntity();
        boolean result = database.removeReactionEntry(fakeEntry);
        assertFalse(result);
    }

}