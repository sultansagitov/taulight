package net.result.taulight.db;

import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.BusyNicknameException;
import net.result.sandnode.security.PasswordHashers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RolesTest {
    private static TauDatabase database;
    private static TauMemberEntity member1;
    private static TauMemberEntity member2;

    @BeforeAll
    public static void setup() throws DatabaseException, BusyNicknameException {
        JPAUtil.buildEntityManagerFactory();
        database = new TauJPADatabase(PasswordHashers.BCRYPT);

        member1 = database.registerMember("user1_roles", "password123").tauMember();
        member2 = database.registerMember("user2_roles", "password123").tauMember();

        assertNotNull(member1.id());
        assertNotNull(member2.id());
    }

    @Test
    void createRole() throws DatabaseException {
        ChannelEntity channel = database.createChannel("role_creation_channel", member1);
        RoleEntity role = database.createRole(channel, "admin");

        assertNotNull(role, "Role should not be null after creation");
        assertEquals("admin", role.name(), "Role name should match the specified name");
        assertEquals(channel, role.channel(), "Role should be associated with the correct channel");
    }

    @Test
    void addMemberToRole() throws DatabaseException {
        ChannelEntity channel = database.createChannel("test_channel", member1);
        RoleEntity role = database.createRole(channel, "moderator");
        boolean result = database.addMemberToRole(role, member2);

        assertTrue(result, "Member should be added to the role");
        assertTrue(channel.roles().contains(role), "Channel should contain the newly created role");
        assertEquals(channel, role.channel(), "Role should be linked to the correct channel");
    }
}
