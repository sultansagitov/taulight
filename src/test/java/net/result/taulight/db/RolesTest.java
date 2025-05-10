package net.result.taulight.db;

import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.BusyNicknameException;
import net.result.sandnode.security.PasswordHashers;
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
        TauDatabase database = new TauJPADatabase(PasswordHashers.BCRYPT);
        channelRepo = new ChannelRepository();
        roleRepo = new RoleRepository();

        member1 = database.registerMember("user1_roles", "password123").tauMember();
        member2 = database.registerMember("user2_roles", "password123").tauMember();

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
