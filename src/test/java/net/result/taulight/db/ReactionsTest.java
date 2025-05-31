package net.result.taulight.db;

import jakarta.persistence.EntityManager;
import net.result.sandnode.GlobalTestState;
import net.result.sandnode.db.MemberRepository;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.BusyNicknameException;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.JPAUtil;
import net.result.taulight.dto.ChatMessageInputDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ReactionsTest {
    private static JPAUtil jpaUtil;
    private static Container container;
    private static EntityManager em;
    private static TauMemberEntity member1;
    private static TauMemberEntity member2;
    private static ChannelRepository channelRepo;
    private static MessageRepository messageRepo;
    private static ReactionPackageRepository reactionPackageRepo;
    private static ReactionTypeRepository reactionTypeRepo;
    private static ReactionEntryRepository reactionEntryRepo;

    @BeforeAll
    public static void setup() throws DatabaseException, BusyNicknameException {
        container = GlobalTestState.container;

        jpaUtil = container.get(JPAUtil.class);

        em = container.get(JPAUtil.class).getEntityManager();

        MemberRepository memberRepo = container.get(MemberRepository.class);
        channelRepo = container.get(ChannelRepository.class);
        messageRepo = container.get(MessageRepository.class);
        reactionPackageRepo = container.get(ReactionPackageRepository.class);
        reactionTypeRepo = container.get(ReactionTypeRepository.class);
        reactionEntryRepo = container.get(ReactionEntryRepository.class);

        member1 = memberRepo.create("user1", "hash").tauMember();
        member2 = memberRepo.create("user2", "hash").tauMember();

        assertNotNull(member1.id());
        assertNotNull(member2.id());
    }

    @Test
    public void createReactionPackage() throws DatabaseException {
        ReactionPackageEntity reactionPackage = reactionPackageRepo.create("funny_emojis", "");
        assertNotNull(reactionPackage);
        assertEquals("funny_emojis", reactionPackage.name());

        ReactionPackageEntity found = em.find(ReactionPackageEntity.class, reactionPackage.id());
        assertNotNull(found);
        assertEquals(reactionPackage.id(), found.id());

        // Additional assertions
        assertNotNull(reactionPackage.id(), "Reaction package ID should not be null");
        assertEquals("", reactionPackage.description(), "Description should match");
        assertEquals(0, reactionPackage.reactionTypes().size(), "New reaction package should have no reaction types");

        // Test creating package with description
        ReactionPackageEntity packageWithDesc = reactionPackageRepo.create("animal_emojis", "Animal themed reactions");
        assertEquals("Animal themed reactions", packageWithDesc.description(), "Description should match");
    }

    @Test
    public void findReactionPackage() throws DatabaseException {
        ReactionPackageEntity created = reactionPackageRepo.create("qwe", "rty");
        Optional<ReactionPackageEntity> found = reactionPackageRepo.find("qwe");
        assertTrue(found.isPresent());
        assertEquals("rty", found.get().description());

        // Additional assertions
        assertEquals(created.id(), found.get().id(), "IDs should match");

        // Test with non-existent package name
        Optional<ReactionPackageEntity> notFound = reactionPackageRepo.find("non_existent_package");
        assertFalse(notFound.isPresent(), "Should not find non-existent package");

        // Create and find another package to ensure multiple packages work
        ReactionPackageEntity second = reactionPackageRepo.create("second_package", "Another package");
        Optional<ReactionPackageEntity> foundSecond = reactionPackageRepo.find("second_package");
        assertTrue(foundSecond.isPresent());
        assertEquals(second.id(), foundSecond.get().id(), "IDs should match for second package");
    }

    @Test
    public void createReactionType1() throws DatabaseException {
        ReactionPackageEntity reactionPackage = reactionPackageRepo.create("standard", "");
        ReactionTypeEntity reactionType = reactionTypeRepo.create("laugh", reactionPackage);

        assertNotNull(reactionType);
        assertEquals("laugh", reactionType.name());
        assertEquals("standard", reactionType.reactionPackage().name());

        ReactionTypeEntity found = container.get(JPAUtil.class).getEntityManager().find(ReactionTypeEntity.class, reactionType.id());
        assertNotNull(found);
        assertEquals("laugh", found.name());

        // Additional assertions
        assertNotNull(reactionType.id(), "Reaction type ID should not be null");
        assertTrue(reactionPackage.reactionTypes().contains(reactionType),
                "Reaction package should contain the new reaction type");

        // Test creating another reaction type in the same package
        ReactionTypeEntity second = reactionTypeRepo.create("cry", reactionPackage);
        assertEquals(reactionPackage, second.reactionPackage(), "Reaction package should match");
        assertEquals(2, reactionPackage.reactionTypes().size(), "Package should now have two reaction types");

        // Test reusing the same name in a different package
        ReactionPackageEntity otherPackage = reactionPackageRepo.create("other_package", "");
        ReactionTypeEntity duplicate = reactionTypeRepo.create("laugh", otherPackage);
        assertEquals("laugh", duplicate.name(), "Should allow same name in different package");
        assertEquals(otherPackage, duplicate.reactionPackage(), "Package should match");
    }

    @Test
    public void createReactionType2() throws DatabaseException {
        ReactionPackageEntity reactionPackage = reactionPackageRepo.create("multi_package", "");

        Collection<String> typeNames = List.of("clap", "wow", "heart");

        Collection<ReactionTypeEntity> createdTypes = reactionTypeRepo.create(reactionPackage, typeNames);

        assertNotNull(createdTypes);
        assertEquals(3, createdTypes.size());

        for (ReactionTypeEntity type : createdTypes) {
            assertTrue(typeNames.contains(type.name()));
            assertEquals(reactionPackage.id(), type.reactionPackage().id());

            ReactionTypeEntity found = container.get(JPAUtil.class).getEntityManager().find(ReactionTypeEntity.class, type.id());
            assertNotNull(found);
            assertEquals(type, found);
        }

        // Additional assertions
        assertEquals(3, reactionPackage.reactionTypes().size(), "Package should have all three reaction types");
        List<String> retrievedNames = reactionPackage.reactionTypes().stream().map(ReactionTypeEntity::name).toList();
        assertTrue(retrievedNames.containsAll(typeNames), "All type names should be present in package");

        // Test adding more types to the same package
        Collection<String> moreTypes = List.of("thumbsup", "thumbsdown");
        Collection<ReactionTypeEntity> moreCreatedTypes = reactionTypeRepo.create(reactionPackage, moreTypes);
        assertEquals(2, moreCreatedTypes.size(), "Should create two more types");
        assertEquals(5, reactionPackage.reactionTypes().size(), "Package should now have five reaction types");
    }

    @Test
    public void createReactionEntry() throws DatabaseException, NotFoundException {
        ReactionPackageEntity testPackage = reactionPackageRepo.create("test", "");
        ReactionTypeEntity reactionType = reactionTypeRepo.create("like", testPackage);

        ChannelEntity channel = channelRepo.create("Test", member1);

        ChatMessageInputDTO input = new ChatMessageInputDTO()
                .setContent("Hello world")
                .setChat(channel)
                .setMember(member1.member())
                .setSentDatetimeNow()
                .setRepliedToMessages(new HashSet<>())
                .setSys(true);

        MessageEntity message = messageRepo.create(channel, input, member1);

        ReactionEntryEntity reactionEntry = reactionEntryRepo.create(member1, message, reactionType);

        assertEquals("Test", ((ChannelEntity) reactionEntry.message().chat()).title());

        // Additional assertions
        assertNotNull(reactionEntry.id(), "Reaction entry ID should not be null");
        assertEquals(member1, reactionEntry.member(), "Member should match");
        assertEquals(message, reactionEntry.message(), "Message should match");
        assertEquals(reactionType, reactionEntry.reactionType(), "Reaction type should match");
        assertTrue(message.reactionEntries().contains(reactionEntry), "Message should contain the reaction entry");

        // Test adding same reaction from different member
        ReactionEntryEntity differentMember = reactionEntryRepo.create(member2, message, reactionType);
        assertEquals(2, message.reactionEntries().size(), "Message should now have two reactions");
        assertEquals(member2, differentMember.member(), "Second reaction should be from member2");

        // Test adding different reaction from same member
        ReactionTypeEntity anotherType = reactionTypeRepo.create("heart", testPackage);
        ReactionEntryEntity differentType = reactionEntryRepo.create(member1, message, anotherType);
        assertEquals(3, message.reactionEntries().size(), "Message should now have three reactions");
        assertEquals(anotherType, differentType.reactionType(), "Third reaction should have different type");
    }

    @Test
    public void removeReactionEntry() throws DatabaseException, NotFoundException {
        ReactionPackageEntity testPackage = reactionPackageRepo.create("test", "");
        ReactionTypeEntity reactionType = reactionTypeRepo.create("fire", testPackage);

        ChannelEntity channel = channelRepo.create("Test", member1);

        ChatMessageInputDTO input = new ChatMessageInputDTO()
                .setContent("Hello world")
                .setChat(channel)
                .setMember(member1.member())
                .setSentDatetimeNow()
                .setRepliedToMessages(new HashSet<>())
                .setSys(true);

        MessageEntity message = messageRepo.create(channel, input, member1);

        ReactionEntryEntity reactionEntry = reactionEntryRepo.create(member1, message, reactionType);

        boolean removed = reactionEntryRepo.delete(reactionEntry);
        assertTrue(removed);

        message = jpaUtil.refresh(message);

        // Additional assertions
        assertEquals(0, message.reactionEntries().size(), "Message should have no reactions after removal");

        // Add multiple reactions and remove one
        ReactionEntryEntity reaction1 = reactionEntryRepo.create(member1, message, reactionType);
        ReactionTypeEntity anotherType = reactionTypeRepo.create("love", testPackage);
        ReactionEntryEntity reaction2 = reactionEntryRepo.create(member2, message, anotherType);

        assertEquals(2, message.reactionEntries().size(), "Message should have two reactions");

        message = jpaUtil.refresh(message);

        boolean removedOne = reactionEntryRepo.delete(reaction1);
        assertTrue(removedOne, "Should successfully remove first reaction");
        assertEquals(1, message.reactionEntries().size(), "Message should have one reaction left");
        assertTrue(message.reactionEntries().contains(reaction2), "Second reaction should still be present");
    }

    @Test
    public void getReactionTypesByPackage() throws DatabaseException {
        ReactionPackageEntity funnyPackage = reactionPackageRepo.create("funny", "");
        ReactionPackageEntity angryPackage = reactionPackageRepo.create("angry_pack", "");

        ReactionTypeEntity smileType = reactionTypeRepo.create("smile", funnyPackage);
        ReactionTypeEntity sadType = reactionTypeRepo.create("sad", funnyPackage);
        ReactionTypeEntity angryType = reactionTypeRepo.create("angry", angryPackage);

        List<ReactionTypeEntity> funnyReactions = reactionTypeRepo.findByPackageName("funny");
        assertEquals(2, funnyReactions.size());

        // Additional assertions
        assertTrue(funnyReactions.contains(smileType), "Should contain smile reaction type");
        assertTrue(funnyReactions.contains(sadType), "Should contain sad reaction type");
        assertFalse(funnyReactions.contains(angryType), "Should not contain angry reaction type");

        // Test another package
        List<ReactionTypeEntity> angryReactions = reactionTypeRepo.findByPackageName("angry_pack");
        assertEquals(1, angryReactions.size(), "Angry package should have one reaction type");
        assertEquals(angryType, angryReactions.get(0), "Should be the angry reaction type");

        // Test non-existent package
        List<ReactionTypeEntity> nonExistentPackage = reactionTypeRepo.findByPackageName("non_existent");
        assertEquals(0, nonExistentPackage.size(), "Non-existent package should return empty list");
    }

    @Test
    public void testRemoveReactionEntry() throws DatabaseException {
        ReactionEntryEntity fakeEntry = new ReactionEntryEntity();
        boolean result = reactionEntryRepo.delete(fakeEntry);
        assertFalse(result);

        // Additional assertions - test with real entry that's already been removed
        ReactionPackageEntity testPackage = reactionPackageRepo.create("test_remove", "");
        ReactionTypeEntity testType = reactionTypeRepo.create("happy", testPackage);
        ChannelEntity channel = channelRepo.create("RemoveChannel", member1);

        ChatMessageInputDTO input = new ChatMessageInputDTO()
                .setContent("Test remove")
                .setChat(channel)
                .setMember(member1.member())
                .setSentDatetimeNow()
                .setRepliedToMessages(new HashSet<>())
                .setSys(false);

        MessageEntity message;
        try {
            message = messageRepo.create(channel, input, member1);
            ReactionEntryEntity entry = reactionEntryRepo.create(member1, message, testType);

            // Remove once
            boolean firstRemove = reactionEntryRepo.delete(entry);
            assertTrue(firstRemove, "First removal should succeed");

            // Try to remove again - should fail
            boolean secondRemove = reactionEntryRepo.delete(entry);
            assertFalse(secondRemove, "Second removal of same entry should fail");

        } catch (NotFoundException e) {
            fail("Should not throw NotFoundException: " + e.getMessage());
        }
    }
}