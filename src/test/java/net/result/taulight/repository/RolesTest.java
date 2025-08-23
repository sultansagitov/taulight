package net.result.taulight.repository;

import net.result.sandnode.GlobalTestState;
import net.result.sandnode.repository.MemberRepository;
import net.result.sandnode.util.Container;
import net.result.taulight.entity.GroupEntity;
import net.result.taulight.entity.RoleEntity;
import net.result.taulight.entity.TauMemberEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RolesTest {
    private static TauMemberEntity member1;
    private static TauMemberEntity member2;
    private static GroupRepository groupRepo;
    private static RoleRepository roleRepo;

    @BeforeAll
    public static void setup() {
        Container container = GlobalTestState.container;
        MemberRepository memberRepo = container.get(MemberRepository.class);
        groupRepo = container.get(GroupRepository.class);
        roleRepo = container.get(RoleRepository.class);

        member1 = memberRepo.create("user1_roles", "hash").getTauMember();
        member2 = memberRepo.create("user2_roles", "hash").getTauMember();

        assertNotNull(member1.id());
        assertNotNull(member2.id());
    }

    @Test
    void createRole() {
        GroupEntity group = groupRepo.create("role_creation_group", member1);
        RoleEntity role = roleRepo.create(group, "admin");

        assertNotNull(role, "Role should not be null after creation");
        assertEquals("admin", role.name(), "Role name should match the specified name");
        assertEquals(group, role.group(), "Role should be associated with the correct group");
    }

    @Test
    void addMemberToRole() {
        GroupEntity group = groupRepo.create("test_group", member1);
        RoleEntity role = roleRepo.create(group, "moderator");
        boolean result = roleRepo.addMember(role, member2);

        assertTrue(result, "Member should be added to the role");
        assertTrue(group.getRoles().contains(role), "Group should contain the newly created role");
        assertEquals(group, role.group(), "Role should be linked to the correct group");
    }
}
