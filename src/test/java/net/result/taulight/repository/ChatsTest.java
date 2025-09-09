package net.result.taulight.repository;

import net.result.sandnode.GlobalTestState;
import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.exception.AlreadyExistingRecordException;
import net.result.sandnode.repository.MemberRepository;
import net.result.sandnode.util.Container;
import net.result.taulight.db.TauMemberCreationListener;
import net.result.taulight.entity.ChatEntity;
import net.result.taulight.entity.DialogEntity;
import net.result.taulight.entity.GroupEntity;
import net.result.taulight.entity.TauMemberEntity;
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
    public static void setup() {
        Container container = GlobalTestState.container;

        container.addInstanceItem(TauMemberCreationListener.class);

        jpaUtil = container.get(JPAUtil.class);

        MemberRepository memberRepo = container.get(MemberRepository.class);
        TauMemberRepository tauMemberRepo = container.get(TauMemberRepository.class);
        dialogRepo = container.get(DialogRepository.class);
        groupRepo = container.get(GroupRepository.class);
        chatUtil = container.get(ChatUtil.class);

        member1 = tauMemberRepo.findByMember(memberRepo.create("user1_chats", "hash"));
        member2 = tauMemberRepo.findByMember(memberRepo.create("user2_chats", "hash"));
        member3 = tauMemberRepo.findByMember(memberRepo.create("user3_chats", "hash"));
        member4 = tauMemberRepo.findByMember(memberRepo.create("user4_chats", "hash"));
        member5 = tauMemberRepo.findByMember(memberRepo.create("user5_chats", "hash"));
        member6 = tauMemberRepo.findByMember(memberRepo.create("user6_chats", "hash"));

        // Assert that all members are properly created
        assertNotNull(member1.id());
        assertNotNull(member2.id());
        assertNotNull(member3.id());
        assertNotNull(member4.id());
        assertNotNull(member5.id());
        assertNotNull(member6.id());
    }

    @Test
    public void createDialog() {
        DialogEntity dialog = dialogRepo.create(member3, member4);
        assertNotNull(dialog);
        if (member3.equals(dialog.getFirstMember())) {
            assertEquals(member3, dialog.getFirstMember());
            assertEquals(member4, dialog.getSecondMember());
        } else {
            assertEquals(member4, dialog.getFirstMember());
            assertEquals(member3, dialog.getSecondMember());
        }

        // Additional assertions
        assertNotNull(dialog.id(), "Dialog ID should not be null");
        assertTrue(member3.getDialogs().contains(dialog), "Dialog should be in member3's dialogs");
        assertTrue(member4.getDialogs().contains(dialog), "Dialog should be in member4's dialogs");
        assertEquals(0, dialog.getMessages().size(), "New dialog should have no messages");

        // Test creating duplicate dialog
        assertThrows(AlreadyExistingRecordException.class, () -> dialogRepo.create(member3, member4),
                "Should not be able to create duplicate dialog");
    }

    @Test
    public void findDialog() {
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
    public void createGroup() {
        GroupEntity group = groupRepo.create("General Chat", member1);

        Optional<ChatEntity> foundGroup = chatUtil.getChat(group.id());
        assertTrue(foundGroup.isPresent());
        assertEquals("General Chat", ((GroupEntity) foundGroup.get()).getTitle());

        member1 = jpaUtil.refresh(member1);

        // Additional assertions
        assertNotNull(group.id(), "Group ID should not be null");
        assertEquals(1, chatUtil.getMembers(group).size(), "Group should have exactly one member (creator)");
        assertTrue(chatUtil.getMembers(group).contains(member1), "Group should contain creator as member");
        assertTrue(member1.getGroups().contains(group), "Member should have group in their groups list");
        assertEquals(0, group.getMessages().size(), "New group should have no messages");
        assertEquals(member1, group.getOwner(), "Group owner should be the creator");
    }

    @Test
    public void getChat() {
        GroupEntity group = groupRepo.create("Test Group", member1);

        Optional<ChatEntity> foundGroup = chatUtil.getChat(group.id());
        assertTrue(foundGroup.isPresent());

        // Additional assertions
        assertEquals(group, foundGroup.get(), "Retrieved group should be the same object");
        assertEquals(group.id(), foundGroup.get().id(), "IDs should match");
        assertEquals("Test Group", ((GroupEntity) foundGroup.get()).getTitle(), "Titles should match");

        // Test with non-existent chat ID
        UUID nonExistentID = UUID.randomUUID();
        Optional<ChatEntity> nonExistentChat = chatUtil.getChat(nonExistentID);
        assertFalse(nonExistentChat.isPresent(), "Should not find non-existent chat");
    }

    @Test
    public void addMemberToGroup() {
        GroupEntity group = groupRepo.create("Test Group", member1);
        member1 = jpaUtil.refresh(member1);

        boolean added = groupRepo.addMember(group, member2);
        group = jpaUtil.refresh(group);
        member2 = jpaUtil.refresh(member2);

        Collection<TauMemberEntity> members = chatUtil.getMembers(group);
        boolean containsMember2 = members.contains(member2);
        boolean containsMember5 = members.contains(member5);
        boolean member2HasGroup = member2.getGroups().contains(group);
        boolean member5HasGroup = member5.getGroups().contains(group);
        int initialCount = members.size();
        boolean containsMember1 = members.contains(member1);

        boolean addedAgain = groupRepo.addMember(group, member2);
        int countAfterDuplicateAdd = chatUtil.getMembers(group).size();

        boolean added3 = groupRepo.addMember(group, member3);
        group = jpaUtil.refresh(group);
        member3 = jpaUtil.refresh(member3);
        Collection<TauMemberEntity> finalMembers = chatUtil.getMembers(group);
        int finalCount = finalMembers.size();
        boolean member3HasGroup = member3.getGroups().contains(group);

        assertTrue(added, "Member2 should be added to the group");
        assertTrue(containsMember2, "Group should contain member2 after addition");
        assertFalse(containsMember5, "Group should not contain member5 (not added)");
        assertTrue(member2HasGroup, "Group should appear in member2's group list");
        assertFalse(member5HasGroup, "Group should not appear in member5's group list");
        assertEquals(2, initialCount, "Group should have exactly two members after first addition");
        assertTrue(containsMember1, "Group should still contain the owner (member1)");

        assertFalse(addedAgain, "Should not be able to add the same member twice");
        assertEquals(2, countAfterDuplicateAdd, "Group size should remain unchanged after duplicate add");

        assertTrue(added3, "Member3 should be added successfully");
        assertEquals(3, finalCount, "Group should have three members after adding member3");
        assertTrue(member3HasGroup, "Group should appear in member3's group list");
    }


    @Test
    public void leaveFromGroup() {
        GroupEntity group = groupRepo.create("Test Group", member1);
        groupRepo.addMember(group, member2);
        group = jpaUtil.refresh(group);

        boolean removed = groupRepo.removeMember(group, member2);
        Collection<TauMemberEntity> membersAfterRemoval = chatUtil.getMembers(group);
        boolean member2StillInGroupList = member2.getGroups().contains(group);
        int memberCountAfterRemoval = membersAfterRemoval.size();
        boolean ownerStillPresent = membersAfterRemoval.contains(member1);
        boolean member2Present = membersAfterRemoval.contains(member2);
        boolean removedAgain = groupRepo.removeMember(group, member2);
        group = jpaUtil.refresh(group);
        boolean ownerRemoved = groupRepo.removeMember(group, member1);
        group = jpaUtil.refresh(group);
        Collection<TauMemberEntity> membersAfterOwnerRemoved = chatUtil.getMembers(group);
        boolean ownerStillInGroupList = member1.getGroups().contains(group);
        int memberCountAfterOwnerRemoved = membersAfterOwnerRemoved.size();

        assertTrue(removed, "Member2 should be successfully removed from the group");
        assertFalse(member2Present, "Member2 should not be in the group after removal");
        assertEquals(1, memberCountAfterRemoval, "Group should have only one member left");
        assertTrue(ownerStillPresent, "Owner should still be in the group");
        assertFalse(member2StillInGroupList, "Group should be removed from member2's group list");
        assertFalse(removedAgain, "Should not be able to remove a member who's not in the group");
        assertTrue(ownerRemoved, "Owner should be able to leave the group");
        assertEquals(0, memberCountAfterOwnerRemoved, "Group should have no members after owner leaves");
        assertFalse(ownerStillInGroupList, "Group should be removed from owner's groups");
    }

    @Test
    public void getMembers() {
        GroupEntity group = groupRepo.create("GetMembersGroup", member1);
        groupRepo.addMember(group, member2);
        group = jpaUtil.refresh(group);

        Collection<TauMemberEntity> members = chatUtil.getMembers(group);
        boolean containsMember1 = members.contains(member1);
        boolean containsMember2 = members.contains(member2);
        boolean containsMember6 = members.contains(member6);
        int memberCount = members.size();

        groupRepo.addMember(group, member3);
        group = jpaUtil.refresh(group);
        member3 = jpaUtil.refresh(member3);
        Collection<TauMemberEntity> updatedMembers = chatUtil.getMembers(group);
        boolean containsMember3 = updatedMembers.contains(member3);
        int updatedCount = updatedMembers.size();

        GroupEntity emptyGroup = groupRepo.create("EmptyGroup", member4);
        Collection<TauMemberEntity> singleMember = chatUtil.getMembers(emptyGroup);
        boolean containsOnlyOwner = singleMember.contains(member4);
        int singleCount = singleMember.size();

        assertTrue(containsMember2, "Group should contain member2 after being added");
        assertFalse(containsMember6, "Group should not contain member6 (never added)");
        assertEquals(2, memberCount, "Group should have exactly two members after adding member2");
        assertTrue(containsMember1, "Group should always contain the creator (member1)");

        assertEquals(3, updatedCount, "Group should have three members after adding member3");
        assertTrue(containsMember3, "Group should contain member3 after being added");

        assertEquals(1, singleCount, "New group should only contain the owner");
        assertTrue(containsOnlyOwner, "New group's member list should include only the owner (member4)");
    }

}
