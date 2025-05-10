package net.result.taulight.db;

import net.result.sandnode.db.Database;
import net.result.sandnode.exception.DatabaseException;

import java.util.*;

/**
 * Interface defining database operations specific to the TauLight module.
 * Extends the generic {@link net.result.sandnode.db.Database} interface.
 */
public interface TauDatabase extends Database {

    /**
     * Retrieves a chat by its ID.
     *
     * @param id the UUID of the chat
     * @return an optional ChatEntity if found
     * @throws DatabaseException if a database error occurs
     */
    Optional<ChatEntity> getChat(UUID id) throws DatabaseException;

    /**
     * Retrieves all members in a given chat.
     *
     * @param chat the chat whose members to retrieve
     * @return a collection of members in the chat
     */
    Collection<TauMemberEntity> getMembers(ChatEntity chat);

    /**
     * Adds a member to the specified role.
     *
     * @param role   the role to which the member should be added
     * @param member the member to add to the role
     * @return true if the member was successfully added to the role, false member already in
     * @throws DatabaseException if a database access error occurs
     */
    boolean addMemberToRole(RoleEntity role, TauMemberEntity member) throws DatabaseException;

    /**
     * Creates a new role within the specified channel.
     *
     * @param channel the channel in which the role will be created
     * @param role    the name of the role to create
     * @return the newly created RoleEntity
     * @throws DatabaseException if a database access error occurs
     */
    RoleEntity createRole(ChannelEntity channel, String role) throws DatabaseException;

}
