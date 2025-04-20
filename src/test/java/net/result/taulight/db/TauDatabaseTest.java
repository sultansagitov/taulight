package net.result.taulight.db;

import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.BusyNicknameException;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.security.PasswordHashers;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.exception.AlreadyExistingRecordException;
import org.junit.jupiter.api.*;

import java.time.ZonedDateTime;
import java.util.*;

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

        // Assert that all members are properly created
        assertNotNull(member1.id());
        assertNotNull(member2.id());
        assertNotNull(member3.id());
        assertNotNull(member4.id());
        assertNotNull(member5.id());
        assertNotNull(member6.id());
    }

    @Test
    public void registerMember() throws DatabaseException, BusyNicknameException {
        MemberEntity newMember = database.registerMember("testuser123", "securePass!");
        assertNotNull(newMember);
        assertEquals("testuser123", newMember.nickname());

        // Additional assertions
        assertNotNull(newMember.id());
        assertNotNull(newMember.tauMember());
        assertEquals(0, newMember.tauMember().dialogs().size(), "New member should have no dialogs");
        assertEquals(0, newMember.tauMember().channels().size(), "New member should have no channels");

        // Test duplicate nickname
        assertThrows(BusyNicknameException.class, () -> database.registerMember("testuser123", "securePass!"));
    }

    @Test
    public void findMemberByNickname() throws DatabaseException, BusyNicknameException {
        MemberEntity registeredMember = database.registerMember("nicksearch", "pass1234");

        Optional<MemberEntity> found = database.findMemberByNickname("nicksearch");
        assertTrue(found.isPresent());
        assertEquals("nicksearch", found.get().nickname());

        // Additional assertions
        assertEquals(registeredMember.id(), found.get().id(), "IDs should match");

        // Test non-existent nickname
        Optional<MemberEntity> notFound = database.findMemberByNickname("nonexistentuser");
        assertFalse(notFound.isPresent(), "Should not find non-existent user");
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

        // Additional assertions
        assertNotNull(dialog.id(), "Dialog ID should not be null");
        assertTrue(member3.dialogs().contains(dialog), "Dialog should be in member3's dialogs");
        assertTrue(member4.dialogs().contains(dialog), "Dialog should be in member4's dialogs");
        assertEquals(0, dialog.messages().size(), "New dialog should have no messages");

        // Test creating duplicate dialog
        assertThrows(AlreadyExistingRecordException.class, () -> database.createDialog(member3, member4),
                "Should not be able to create duplicate dialog");
    }

    @Test
    public void findDialog() throws DatabaseException, AlreadyExistingRecordException {
        DialogEntity createdDialog = database.createDialog(member5, member6);
        Optional<DialogEntity> foundDialog = database.findDialog(member5, member6);

        assertTrue(foundDialog.isPresent());
        assertEquals(createdDialog.id(), foundDialog.get().id(), "Dialog IDs should match");

        // Test find in reverse order
        Optional<DialogEntity> reverseFindDialog = database.findDialog(member6, member5);
        assertTrue(reverseFindDialog.isPresent());
        assertEquals(createdDialog.id(), reverseFindDialog.get().id(), "Should find same dialog regardless of member order");

        // Test with non-existent dialog
        Optional<DialogEntity> nonExistentDialog = database.findDialog(member1, member5);
        assertFalse(nonExistentDialog.isPresent(), "Should not find dialog between unrelated members");
    }

    @Test
    public void createChannel() throws DatabaseException {
        ChannelEntity channel = database.createChannel("General Chat", member1);

        Optional<ChatEntity> foundChannel = database.getChat(channel.id());
        assertTrue(foundChannel.isPresent());
        assertEquals("General Chat", ((ChannelEntity) foundChannel.get()).title());

        // Additional assertions
        assertNotNull(channel.id(), "Channel ID should not be null");
        assertEquals(1, database.getMembers(channel).size(), "Channel should have exactly one member (creator)");
        assertTrue(database.getMembers(channel).contains(member1), "Channel should contain creator as member");
        assertTrue(member1.channels().contains(channel), "Member should have channel in their channels list");
        assertEquals(0, channel.messages().size(), "New channel should have no messages");
        assertEquals(member1, channel.owner(), "Channel owner should be the creator");
    }

    @Test
    public void getChat() throws DatabaseException {
        ChannelEntity channel = database.createChannel("Test Channel", member1);

        Optional<ChatEntity> foundChannel = database.getChat(channel.id());
        assertTrue(foundChannel.isPresent());

        // Additional assertions
        assertSame(channel, foundChannel.get(), "Retrieved channel should be the same object");
        assertEquals(channel.id(), foundChannel.get().id(), "IDs should match");
        assertEquals("Test Channel", ((ChannelEntity) foundChannel.get()).title(), "Titles should match");

        // Test with non-existent chat ID
        UUID nonExistentID = UUID.randomUUID();
        Optional<ChatEntity> nonExistentChat = database.getChat(nonExistentID);
        assertFalse(nonExistentChat.isPresent(), "Should not find non-existent chat");
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
    public void addMemberToChannel() throws DatabaseException {
        ChannelEntity channel = database.createChannel("Test Channel", member1);

        boolean added = database.addMemberToChannel(channel, member2);
        assertTrue(added);

        Collection<TauMemberEntity> members = database.getMembers(channel);
        assertTrue(members.contains(member2));
        assertFalse(members.contains(member5));

        assertTrue(member2.channels().contains(channel));
        assertFalse(member5.channels().contains(channel));

        // Additional assertions
        assertEquals(2, members.size(), "Channel should have exactly two members");
        assertTrue(members.contains(member1), "Channel should still contain the owner");

        // Test adding the same member again
        boolean addedAgain = database.addMemberToChannel(channel, member2);
        assertFalse(addedAgain, "Should not add the same member twice");
        assertEquals(2, database.getMembers(channel).size(), "Member count should not change");

        // Add a third member and verify
        boolean added3 = database.addMemberToChannel(channel, member3);
        assertTrue(added3, "Should add third member successfully");
        assertEquals(3, database.getMembers(channel).size(), "Channel should now have three members");
        assertTrue(member3.channels().contains(channel), "Channel should be in member3's channels");
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

    @Test
    public void leaveFromChannel() throws DatabaseException {
        ChannelEntity channel = database.createChannel("Test Channel", member1);

        database.addMemberToChannel(channel, member2);
        boolean removed = database.leaveFromChannel(channel, member2);
        assertTrue(removed);

        Collection<TauMemberEntity> members = database.getMembers(channel);
        assertFalse(members.contains(member2));

        // Additional assertions
        assertEquals(1, members.size(), "Channel should have only one member left");
        assertTrue(members.contains(member1), "Owner should still be in the channel");
        assertFalse(member2.channels().contains(channel), "Channel should be removed from member2's channels");

        // Test removing a member who's not in the channel
        boolean removedAgain = database.leaveFromChannel(channel, member2);
        assertFalse(removedAgain, "Should not be able to remove a member who's not in the channel");

        // Test removing the owner
        boolean ownerRemoved = database.leaveFromChannel(channel, member1);
        assertTrue(ownerRemoved, "Owner should be able to leave the channel");
        assertEquals(0, database.getMembers(channel).size(), "Channel should have no members after owner leaves");
        assertFalse(member1.channels().contains(channel), "Channel should be removed from owner's channels");
    }

    @Test
    public void createInviteCode() throws DatabaseException {
        ChannelEntity channel = database.createChannel("Test Channel", member1);

        ZonedDateTime expiresDate = ZonedDateTime.now().plusDays(1);
        InviteCodeEntity inviteCode = database.createInviteCode(channel, member2, member1, expiresDate);

        String stringCode = inviteCode.code();
        Optional<InviteCodeEntity> foundInviteCode = database.findInviteCode(stringCode);
        assertTrue(foundInviteCode.isPresent());
        assertEquals(stringCode, foundInviteCode.get().code());

        // Additional assertions
        assertNotNull(inviteCode.id(), "Invite code ID should not be null");
        assertEquals(channel, inviteCode.channel(), "Invite should be for the correct channel");
        assertEquals(member1, inviteCode.sender(), "Sender should be member1");
        assertEquals(member2, inviteCode.receiver(), "Receiver should be member2");
        assertEquals(expiresDate.toEpochSecond(), inviteCode.expiresDate().toEpochSecond(), "Expiration date should match");
        assertNull(inviteCode.activationDate(), "New invite code should not be activated");
    }

    @Test
    public void findInviteCode1() throws DatabaseException {
        ChannelEntity channel = database.createChannel("InviteChannel", member1);

        ZonedDateTime expiration = ZonedDateTime.now().plusDays(1);
        InviteCodeEntity invite = database.createInviteCode(channel, member2, member1, expiration);

        Optional<InviteCodeEntity> result = database.findInviteCode(invite.code());
        assertTrue(result.isPresent());
        assertEquals(invite, result.get());

        // Additional assertions
        assertEquals(invite.id(), result.get().id(), "IDs should match");
        assertEquals(invite.channel(), result.get().channel(), "Channels should match");
        assertEquals(invite.receiver(), result.get().receiver(), "Receivers should match");
        assertEquals(invite.sender(), result.get().sender(), "Senders should match");

        // Test with non-existent invite code
        Optional<InviteCodeEntity> nonExistentInvite = database.findInviteCode("non-existent-code");
        assertFalse(nonExistentInvite.isPresent(), "Should not find non-existent invite code");
    }

    @Test
    public void findInviteCode2() throws DatabaseException {
        ChannelEntity channel = database.createChannel("InviteChannel", member3);

        ZonedDateTime expiration = ZonedDateTime.now().plusDays(1);
        InviteCodeEntity invite1 = database.createInviteCode(channel, member4, member3, expiration);
        InviteCodeEntity invite2 = database.createInviteCode(channel, member4, member3, expiration);

        Collection<InviteCodeEntity> result = database.findInviteCode(channel, member4);
        assertEquals(2, result.size());

        // Additional assertions
        List<String> inviteCodes = result.stream().map(InviteCodeEntity::code).toList();
        assertTrue(inviteCodes.contains(invite1.code()), "Result should contain first invite code");
        assertTrue(inviteCodes.contains(invite2.code()), "Result should contain second invite code");

        // Test with a different receiver
        InviteCodeEntity invite3 = database.createInviteCode(channel, member5, member3, expiration);
        Collection<InviteCodeEntity> result2 = database.findInviteCode(channel, member5);
        assertEquals(1, result2.size(), "Should find only one invite from member5");
        assertEquals(invite3.code(), result2.iterator().next().code(), "Should find the correct invite");

        // Test with no invites
        Collection<InviteCodeEntity> emptyResult = database.findInviteCode(channel, member6);
        assertEquals(0, emptyResult.size(), "Should find no invites for member6");
    }

    @Test
    public void activateInviteCode() throws DatabaseException {
        ChannelEntity channel = database.createChannel("Test Channel", member1);

        ZonedDateTime expiresDate = ZonedDateTime.now().plusDays(1);
        InviteCodeEntity inviteCode = database.createInviteCode(channel, member1, member2, expiresDate);

        boolean activated = database.activateInviteCode(inviteCode);
        assertTrue(activated);

        // Additional assertions
        Optional<InviteCodeEntity> found = database.findInviteCode(inviteCode.code());
        assertTrue(found.isPresent(), "Should still find the invite code after activation");
        assertNotNull(found.get().activationDate(), "Invite code should be marked as activated");

        // Test activating an already activated code
        boolean activatedAgain = database.activateInviteCode(inviteCode);
        assertFalse(activatedAgain, "Should not activate an already activated invite code");

        // Test with an expired invite code
        ZonedDateTime pastDate = ZonedDateTime.now().minusDays(1);
        InviteCodeEntity expiredInvite = database.createInviteCode(channel, member1, member3, pastDate);
        boolean activatedExpired = database.activateInviteCode(expiredInvite);
        assertFalse(activatedExpired, "Should not activate an expired invite code");
    }

    @Test
    public void createReactionPackage() throws DatabaseException {
        ReactionPackageEntity reactionPackage = database.createReactionPackage("funny_emojis", "");
        assertNotNull(reactionPackage);
        assertEquals("funny_emojis", reactionPackage.name());

        ReactionPackageEntity found = JPAUtil.getEntityManager().find(ReactionPackageEntity.class, reactionPackage.id());
        assertNotNull(found);
        assertEquals(reactionPackage.id(), found.id());

        // Additional assertions
        assertNotNull(reactionPackage.id(), "Reaction package ID should not be null");
        assertEquals("", reactionPackage.description(), "Description should match");
        assertEquals(0, reactionPackage.reactionTypes().size(), "New reaction package should have no reaction types");

        // Test creating package with description
        ReactionPackageEntity packageWithDesc = database.createReactionPackage("animal_emojis", "Animal themed reactions");
        assertEquals("Animal themed reactions", packageWithDesc.description(), "Description should match");
    }

    @Test
    public void findReactionPackage() throws DatabaseException {
        ReactionPackageEntity created = database.createReactionPackage("qwe", "rty");
        Optional<ReactionPackageEntity> found = database.findReactionPackage("qwe");
        assertTrue(found.isPresent());
        assertEquals("rty", found.get().description());

        // Additional assertions
        assertEquals(created.id(), found.get().id(), "IDs should match");

        // Test with non-existent package name
        Optional<ReactionPackageEntity> notFound = database.findReactionPackage("non_existent_package");
        assertFalse(notFound.isPresent(), "Should not find non-existent package");

        // Create and find another package to ensure multiple packages work
        ReactionPackageEntity second = database.createReactionPackage("second_package", "Another package");
        Optional<ReactionPackageEntity> foundSecond = database.findReactionPackage("second_package");
        assertTrue(foundSecond.isPresent());
        assertEquals(second.id(), foundSecond.get().id(), "IDs should match for second package");
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

        // Additional assertions
        assertNotNull(reactionType.id(), "Reaction type ID should not be null");
        assertTrue(reactionPackage.reactionTypes().contains(reactionType),
                "Reaction package should contain the new reaction type");

        // Test creating another reaction type in the same package
        ReactionTypeEntity second = database.createReactionType("cry", reactionPackage);
        assertEquals(reactionPackage, second.reactionPackage(), "Reaction package should match");
        assertEquals(2, reactionPackage.reactionTypes().size(), "Package should now have two reaction types");

        // Test reusing the same name in a different package
        ReactionPackageEntity otherPackage = database.createReactionPackage("other_package", "");
        ReactionTypeEntity duplicate = database.createReactionType("laugh", otherPackage);
        assertEquals("laugh", duplicate.name(), "Should allow same name in different package");
        assertEquals(otherPackage, duplicate.reactionPackage(), "Package should match");
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

        // Additional assertions
        assertEquals(3, reactionPackage.reactionTypes().size(), "Package should have all three reaction types");
        List<String> retrievedNames = reactionPackage.reactionTypes().stream().map(ReactionTypeEntity::name).toList();
        assertTrue(retrievedNames.containsAll(typeNames), "All type names should be present in package");

        // Test adding more types to the same package
        Collection<String> moreTypes = List.of("thumbsup", "thumbsdown");
        Collection<ReactionTypeEntity> moreCreatedTypes = database.createReactionType(reactionPackage, moreTypes);
        assertEquals(2, moreCreatedTypes.size(), "Should create two more types");
        assertEquals(5, reactionPackage.reactionTypes().size(), "Package should now have five reaction types");
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

        // Additional assertions
        assertNotNull(reactionEntry.id(), "Reaction entry ID should not be null");
        assertEquals(member1, reactionEntry.member(), "Member should match");
        assertEquals(message, reactionEntry.message(), "Message should match");
        assertEquals(reactionType, reactionEntry.reactionType(), "Reaction type should match");
        assertTrue(message.reactionEntries().contains(reactionEntry), "Message should contain the reaction entry");

        // Test adding same reaction from different member
        ReactionEntryEntity differentMember = database.createReactionEntry(member2, message, reactionType);
        assertEquals(2, message.reactionEntries().size(), "Message should now have two reactions");
        assertEquals(member2, differentMember.member(), "Second reaction should be from member2");

        // Test adding different reaction from same member
        ReactionTypeEntity anotherType = database.createReactionType("heart", testPackage);
        ReactionEntryEntity differentType = database.createReactionEntry(member1, message, anotherType);
        assertEquals(3, message.reactionEntries().size(), "Message should now have three reactions");
        assertEquals(anotherType, differentType.reactionType(), "Third reaction should have different type");
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

        // Additional assertions
        assertEquals(0, message.reactionEntries().size(), "Message should have no reactions after removal");

        // Add multiple reactions and remove one
        ReactionEntryEntity reaction1 = database.createReactionEntry(member1, message, reactionType);
        ReactionTypeEntity anotherType = database.createReactionType("love", testPackage);
        ReactionEntryEntity reaction2 = database.createReactionEntry(member2, message, anotherType);

        assertEquals(2, message.reactionEntries().size(), "Message should have two reactions");

        boolean removedOne = database.removeReactionEntry(reaction1);
        assertTrue(removedOne, "Should successfully remove first reaction");
        assertEquals(1, message.reactionEntries().size(), "Message should have one reaction left");
        assertTrue(message.reactionEntries().contains(reaction2), "Second reaction should still be present");
    }

    @Test
    public void getReactionTypesByPackage() throws DatabaseException {
        ReactionPackageEntity funnyPackage = database.createReactionPackage("funny", "");
        ReactionPackageEntity angryPackage = database.createReactionPackage("angry_pack", "");

        ReactionTypeEntity smileType = database.createReactionType("smile", funnyPackage);
        ReactionTypeEntity sadType = database.createReactionType("sad", funnyPackage);
        ReactionTypeEntity angryType = database.createReactionType("angry", angryPackage);

        List<ReactionTypeEntity> funnyReactions = database.getReactionTypesByPackage("funny");
        assertEquals(2, funnyReactions.size());

        // Additional assertions
        assertTrue(funnyReactions.contains(smileType), "Should contain smile reaction type");
        assertTrue(funnyReactions.contains(sadType), "Should contain sad reaction type");
        assertFalse(funnyReactions.contains(angryType), "Should not contain angry reaction type");

        // Test another package
        List<ReactionTypeEntity> angryReactions = database.getReactionTypesByPackage("angry_pack");
        assertEquals(1, angryReactions.size(), "Angry package should have one reaction type");
        assertEquals(angryType, angryReactions.get(0), "Should be the angry reaction type");

        // Test non-existent package
        List<ReactionTypeEntity> nonExistentPackage = database.getReactionTypesByPackage("non_existent");
        assertEquals(0, nonExistentPackage.size(), "Non-existent package should return empty list");
    }

    @Test
    public void getMembers() throws DatabaseException {
        ChannelEntity channel = database.createChannel("GetMembersChannel", member1);
        database.addMemberToChannel(channel, member2);

        Collection<TauMemberEntity> members = database.getMembers(channel);
        assertTrue(members.contains(member2));
        assertFalse(members.contains(member6));

        // Additional assertions
        assertEquals(2, members.size(), "Channel should have exactly two members");
        assertTrue(members.contains(member1), "Channel should contain its creator");

        // Add another member and check again
        database.addMemberToChannel(channel, member3);
        Collection<TauMemberEntity> updatedMembers = database.getMembers(channel);
        assertEquals(3, updatedMembers.size(), "Channel should now have three members");
        assertTrue(updatedMembers.contains(member3), "New member should be in the channel");

        // Test with empty channel (except owner)
        ChannelEntity emptyChannel = database.createChannel("EmptyChannel", member4);
        Collection<TauMemberEntity> singleMember = database.getMembers(emptyChannel);
        assertEquals(1, singleMember.size(), "Empty channel should have just the owner");
        assertTrue(singleMember.contains(member4), "Owner should be in the member list");
    }

    @Test
    public void testRemoveReactionEntry() throws DatabaseException {
        ReactionEntryEntity fakeEntry = new ReactionEntryEntity();
        boolean result = database.removeReactionEntry(fakeEntry);
        assertFalse(result);

        // Additional assertions - test with real entry that's already been removed
        ReactionPackageEntity testPackage = database.createReactionPackage("test_remove", "");
        ReactionTypeEntity testType = database.createReactionType("happy", testPackage);
        ChannelEntity channel = database.createChannel("RemoveChannel", member1);

        ChatMessageInputDTO input = new ChatMessageInputDTO()
                .setContent("Test remove")
                .setChat(channel)
                .setMember(member1.member())
                .setSentDatetimeNow()
                .setRepliedToMessages(new HashSet<>())
                .setSys(false);

        MessageEntity message;
        try {
            message = database.createMessage(channel, input, member1);
            ReactionEntryEntity entry = database.createReactionEntry(member1, message, testType);

            // Remove once
            boolean firstRemove = database.removeReactionEntry(entry);
            assertTrue(firstRemove, "First removal should succeed");

            // Try to remove again - should fail
            boolean secondRemove = database.removeReactionEntry(entry);
            assertFalse(secondRemove, "Second removal of same entry should fail");

        } catch (NotFoundException e) {
            fail("Should not throw NotFoundException: " + e.getMessage());
        }
    }

}