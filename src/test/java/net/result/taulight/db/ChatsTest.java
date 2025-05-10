package net.result.taulight.db;

import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.db.MemberRepository;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.BusyNicknameException;
import net.result.sandnode.security.PasswordHashers;
import net.result.taulight.exception.AlreadyExistingRecordException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChatsTest {
    private static TauDatabase database;
    private static TauMemberEntity member1;
    private static TauMemberEntity member2;
    private static TauMemberEntity member3;
    private static TauMemberEntity member4;
    private static TauMemberEntity member5;
    private static TauMemberEntity member6;
    private static DialogRepository dialogRepo;
    private static ChannelRepository channelRepo;

    @BeforeAll
    public static void setup() throws DatabaseException, BusyNicknameException {
        JPAUtil.buildEntityManagerFactory();

        database = new TauJPADatabase();
        MemberRepository memberRepo = new MemberRepository();
        dialogRepo = new DialogRepository();
        channelRepo = new ChannelRepository();

        member1 = memberRepo.create("user1_chats", PasswordHashers.BCRYPT.hash("password123", 12)).tauMember();
        member2 = memberRepo.create("user2_chats", PasswordHashers.BCRYPT.hash("password123", 12)).tauMember();
        member3 = memberRepo.create("user3_chats", PasswordHashers.BCRYPT.hash("password123", 12)).tauMember();
        member4 = memberRepo.create("user4_chats", PasswordHashers.BCRYPT.hash("password123", 12)).tauMember();
        member5 = memberRepo.create("user5_chats", PasswordHashers.BCRYPT.hash("password123", 12)).tauMember();
        member6 = memberRepo.create("user6_chats", PasswordHashers.BCRYPT.hash("password123", 12)).tauMember();

        // Assert that all members are properly created
        assertNotNull(member1.id());
        assertNotNull(member2.id());
        assertNotNull(member3.id());
        assertNotNull(member4.id());
        assertNotNull(member5.id());
        assertNotNull(member6.id());
    }

    @Test
    public void createDialog() throws DatabaseException, AlreadyExistingRecordException {
        DialogEntity dialog = dialogRepo.create(member3, member4);
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
        assertThrows(AlreadyExistingRecordException.class, () -> dialogRepo.create(member3, member4),
                "Should not be able to create duplicate dialog");
    }

    @Test
    public void findDialog() throws DatabaseException, AlreadyExistingRecordException {
        DialogEntity createdDialog = dialogRepo.create(member5, member6);
        Optional<DialogEntity> foundDialog = dialogRepo.findByMembers(member5, member6);

        assertTrue(foundDialog.isPresent());
        assertEquals(createdDialog.id(), foundDialog.get().id(), "Dialog IDs should match");

        // Test find in reverse order
        Optional<DialogEntity> reverseFindDialog = dialogRepo.findByMembers(member6, member5);
        assertTrue(reverseFindDialog.isPresent());
        assertEquals(createdDialog.id(), reverseFindDialog.get().id(), "Should find same dialog regardless of member order");

        // Test with non-existent dialog
        Optional<DialogEntity> nonExistentDialog = dialogRepo.findByMembers(member1, member5);
        assertFalse(nonExistentDialog.isPresent(), "Should not find dialog between unrelated members");
    }

    @Test
    public void createChannel() throws DatabaseException {
        ChannelEntity channel = channelRepo.create("General Chat", member1);

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
        ChannelEntity channel = channelRepo.create("Test Channel", member1);

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
    public void addMemberToChannel() throws DatabaseException {
        ChannelEntity channel = channelRepo.create("Test Channel", member1);

        boolean added = channelRepo.addMember(channel, member2);
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
        boolean addedAgain = channelRepo.addMember(channel, member2);
        assertFalse(addedAgain, "Should not add the same member twice");
        assertEquals(2, database.getMembers(channel).size(), "Member count should not change");

        // Add a third member and verify
        boolean added3 = channelRepo.addMember(channel, member3);
        assertTrue(added3, "Should add third member successfully");
        assertEquals(3, database.getMembers(channel).size(), "Channel should now have three members");
        assertTrue(member3.channels().contains(channel), "Channel should be in member3's channels");
    }

    @Test
    public void leaveFromChannel() throws DatabaseException {
        ChannelEntity channel = channelRepo.create("Test Channel", member1);

        channelRepo.addMember(channel, member2);
        boolean removed = channelRepo.removeMember(channel, member2);
        assertTrue(removed);

        Collection<TauMemberEntity> members = database.getMembers(channel);
        assertFalse(members.contains(member2));

        // Additional assertions
        assertEquals(1, members.size(), "Channel should have only one member left");
        assertTrue(members.contains(member1), "Owner should still be in the channel");
        assertFalse(member2.channels().contains(channel), "Channel should be removed from member2's channels");

        // Test removing a member who's not in the channel
        boolean removedAgain = channelRepo.removeMember(channel, member2);
        assertFalse(removedAgain, "Should not be able to remove a member who's not in the channel");

        // Test removing the owner
        boolean ownerRemoved = channelRepo.removeMember(channel, member1);
        assertTrue(ownerRemoved, "Owner should be able to leave the channel");
        assertEquals(0, database.getMembers(channel).size(), "Channel should have no members after owner leaves");
        assertFalse(member1.channels().contains(channel), "Channel should be removed from owner's channels");
    }

    @Test
    public void getMembers() throws DatabaseException {
        ChannelEntity channel = channelRepo.create("GetMembersChannel", member1);
        channelRepo.addMember(channel, member2);

        Collection<TauMemberEntity> members = database.getMembers(channel);
        assertTrue(members.contains(member2));
        assertFalse(members.contains(member6));

        // Additional assertions
        assertEquals(2, members.size(), "Channel should have exactly two members");
        assertTrue(members.contains(member1), "Channel should contain its creator");

        // Add another member and check again
        channelRepo.addMember(channel, member3);
        Collection<TauMemberEntity> updatedMembers = database.getMembers(channel);
        assertEquals(3, updatedMembers.size(), "Channel should now have three members");
        assertTrue(updatedMembers.contains(member3), "New member should be in the channel");

        // Test with empty channel (except owner)
        ChannelEntity emptyChannel = channelRepo.create("EmptyChannel", member4);
        Collection<TauMemberEntity> singleMember = database.getMembers(emptyChannel);
        assertEquals(1, singleMember.size(), "Empty channel should have just the owner");
        assertTrue(singleMember.contains(member4), "Owner should be in the member list");
    }

}
