package net.result.taulight.db;

import net.result.sandnode.GlobalTestState;
import net.result.sandnode.db.MemberRepository;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.BusyNicknameException;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.JPAUtil;
import net.result.sandnode.exception.AlreadyExistingRecordException;
import net.result.taulight.util.ChatUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ChatsTest {
    private static JPAUtil jpaUtil;
    private static ChatUtil chatUtil;
    private static TauMemberEntity member1;
    private static TauMemberEntity member2;
    private static TauMemberEntity member3;
    private static TauMemberEntity member4;
    private static TauMemberEntity member5;
    private static TauMemberEntity member6;
    private static DialogRepository dialogRepo;
    private static GroupRepository groupRepo;

    @BeforeAll
    public static void setup() throws DatabaseException, BusyNicknameException {
        Container container = GlobalTestState.container;

        jpaUtil = container.get(JPAUtil.class);

        MemberRepository memberRepo = container.get(MemberRepository.class);
        dialogRepo = container.get(DialogRepository.class);
        groupRepo = container.get(GroupRepository.class);
        chatUtil = container.get(ChatUtil.class);

        member1 = memberRepo.create("user1_chats", "hash").tauMember();
        member2 = memberRepo.create("user2_chats", "hash").tauMember();
        member3 = memberRepo.create("user3_chats", "hash").tauMember();
        member4 = memberRepo.create("user4_chats", "hash").tauMember();
        member5 = memberRepo.create("user5_chats", "hash").tauMember();
        member6 = memberRepo.create("user6_chats", "hash").tauMember();

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
        if (member3.equals(dialog.firstMember())) {
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
        assertEquals(createdDialog, foundDialog.get(), "Dialogs should match");

        // Test find in reverse order
        Optional<DialogEntity> reverseFindDialog = dialogRepo.findByMembers(member6, member5);
        assertTrue(reverseFindDialog.isPresent());
        assertEquals(createdDialog, reverseFindDialog.get(), "Should find same dialog regardless of member order");

        // Test with non-existent dialog
        Optional<DialogEntity> nonExistentDialog = dialogRepo.findByMembers(member1, member5);
        assertFalse(nonExistentDialog.isPresent(), "Should not find dialog between unrelated members");
    }

    @Test
    public void createGroup() throws DatabaseException {
        GroupEntity group = groupRepo.create("General Chat", member1);

        Optional<ChatEntity> foundGroup = chatUtil.getChat(group.id());
        assertTrue(foundGroup.isPresent());
        assertEquals("General Chat", ((GroupEntity) foundGroup.get()).title());

        member1 = jpaUtil.refresh(member1);

        // Additional assertions
        assertNotNull(group.id(), "Group ID should not be null");
        assertEquals(1, chatUtil.getMembers(group).size(), "Group should have exactly one member (creator)");
        assertTrue(chatUtil.getMembers(group).contains(member1), "Group should contain creator as member");
        assertTrue(member1.groups().contains(group), "Member should have group in their groups list");
        assertEquals(0, group.messages().size(), "New group should have no messages");
        assertEquals(member1, group.owner(), "Group owner should be the creator");
    }

    @Test
    public void getChat() throws DatabaseException {
        GroupEntity group = groupRepo.create("Test Group", member1);

        Optional<ChatEntity> foundGroup = chatUtil.getChat(group.id());
        assertTrue(foundGroup.isPresent());

        // Additional assertions
        assertEquals(group, foundGroup.get(), "Retrieved group should be the same object");
        assertEquals(group.id(), foundGroup.get().id(), "IDs should match");
        assertEquals("Test Group", ((GroupEntity) foundGroup.get()).title(), "Titles should match");

        // Test with non-existent chat ID
        UUID nonExistentID = UUID.randomUUID();
        Optional<ChatEntity> nonExistentChat = chatUtil.getChat(nonExistentID);
        assertFalse(nonExistentChat.isPresent(), "Should not find non-existent chat");
    }

    @Test
    public void addMemberToGroup() throws DatabaseException {
        GroupEntity group = groupRepo.create("Test Group", member1);

        boolean added = groupRepo.addMember(group, member2);
        assertTrue(added);

        Collection<TauMemberEntity> members = chatUtil.getMembers(group);
        assertTrue(members.contains(member2));
        assertFalse(members.contains(member5));

        assertTrue(member2.groups().contains(group));
        assertFalse(member5.groups().contains(group));

        // Additional assertions
        assertEquals(2, members.size(), "Group should have exactly two members");
        assertTrue(members.contains(member1), "Group should still contain the owner");

        // Test adding the same member again
        boolean addedAgain = groupRepo.addMember(group, member2);
        assertFalse(addedAgain, "Should not add the same member twice");
        assertEquals(2, chatUtil.getMembers(group).size(), "Member count should not change");

        // Add a third member and verify
        boolean added3 = groupRepo.addMember(group, member3);
        assertTrue(added3, "Should add third member successfully");
        assertEquals(3, chatUtil.getMembers(group).size(), "Group should now have three members");
        assertTrue(member3.groups().contains(group), "Group should be in member3's groups");
    }

    @Test
    public void leaveFromGroup() throws DatabaseException {
        GroupEntity group = groupRepo.create("Test Group", member1);

        groupRepo.addMember(group, member2);
        boolean removed = groupRepo.removeMember(group, member2);
        assertTrue(removed);

        Collection<TauMemberEntity> members = chatUtil.getMembers(group);
        assertFalse(members.contains(member2));

        // Additional assertions
        assertEquals(1, members.size(), "Group should have only one member left");
        assertTrue(members.contains(member1), "Owner should still be in the group");
        assertFalse(member2.groups().contains(group), "Group should be removed from member2's groups");

        // Test removing a member who's not in the group
        boolean removedAgain = groupRepo.removeMember(group, member2);
        assertFalse(removedAgain, "Should not be able to remove a member who's not in the group");

        // Test removing the owner
        boolean ownerRemoved = groupRepo.removeMember(group, member1);
        assertTrue(ownerRemoved, "Owner should be able to leave the group");
        assertEquals(0, chatUtil.getMembers(group).size(), "Group should have no members after owner leaves");
        assertFalse(member1.groups().contains(group), "Group should be removed from owner's groups");
    }

    @Test
    public void getMembers() throws DatabaseException {
        GroupEntity group = groupRepo.create("GetMembersGroup", member1);
        groupRepo.addMember(group, member2);

        Collection<TauMemberEntity> members = chatUtil.getMembers(group);
        assertTrue(members.contains(member2));
        assertFalse(members.contains(member6));

        // Additional assertions
        assertEquals(2, members.size(), "Group should have exactly two members");
        assertTrue(members.contains(member1), "Group should contain its creator");

        // Add another member and check again
        groupRepo.addMember(group, member3);
        Collection<TauMemberEntity> updatedMembers = chatUtil.getMembers(group);
        assertEquals(3, updatedMembers.size(), "Group should now have three members");
        assertTrue(updatedMembers.contains(member3), "New member should be in the group");

        // Test with empty group (except owner)
        GroupEntity emptyGroup = groupRepo.create("EmptyGroup", member4);
        Collection<TauMemberEntity> singleMember = chatUtil.getMembers(emptyGroup);
        assertEquals(1, singleMember.size(), "Empty group should have just the owner");
        assertTrue(singleMember.contains(member4), "Owner should be in the member list");
    }
}
