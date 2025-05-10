package net.result.taulight.db;

import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.db.MemberRepository;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.BusyNicknameException;
import net.result.sandnode.security.PasswordHashers;
import net.result.sandnode.util.Container;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RolesTest {
    private static TauMemberEntity member1;
    private static TauMemberEntity member2;
    private static ChannelRepository channelRepo;
    private static RoleRepository roleRepo;

    @BeforeAll
    public static void setup() throws DatabaseException, BusyNicknameException {
        JPAUtil.buildEntityManagerFactory();
        Container container = new Container();
        MemberRepository memberRepo = container.get(MemberRepository.class);
        channelRepo = container.get(ChannelRepository.class);
        roleRepo = container.get(RoleRepository.class);

        member1 = memberRepo.create("user1_roles", PasswordHashers.BCRYPT.hash("password123", 12)).tauMember();
        member2 = memberRepo.create("user2_roles", PasswordHashers.BCRYPT.hash("password123", 12)).tauMember();

        assertNotNull(member1.id());
        assertNotNull(member2.id());
    }

    @Test
    void createRole() throws DatabaseException {
        ChannelEntity channel = channelRepo.create("role_creation_channel", member1);
        RoleEntity role = roleRepo.create(channel, "admin");

        assertNotNull(role, "Role should not be null after creation");
        assertEquals("admin", role.name(), "Role name should match the specified name");
        assertEquals(channel, role.channel(), "Role should be associated with the correct channel");
    }

    @Test
    void addMemberToRole() throws DatabaseException {
        ChannelEntity channel = channelRepo.create("test_channel", member1);
        RoleEntity role = roleRepo.create(channel, "moderator");
        boolean result = roleRepo.addMember(role, member2);

        assertTrue(result, "Member should be added to the role");
        assertTrue(channel.roles().contains(role), "Channel should contain the newly created role");
        assertEquals(channel, role.channel(), "Role should be linked to the correct channel");
    }
}
