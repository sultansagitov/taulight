package net.result.taulight.db;

import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.BusyNicknameException;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.security.PasswordHashers;
import net.result.taulight.dto.ChatMessageInputDTO;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ReactionsTest {

    private static TauDatabase database;
    private static TauMemberEntity member1;
    private static TauMemberEntity member2;

    @BeforeAll
    public static void setup() throws DatabaseException, BusyNicknameException {
        JPAUtil.buildEntityManagerFactory();
        database = new TauJPADatabase(PasswordHashers.BCRYPT);

        member1 = database.registerMember("user1", "password123").tauMember();
        member2 = database.registerMember("user2", "password123").tauMember();

        assertNotNull(member1.id());
        assertNotNull(member2.id());
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