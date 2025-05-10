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

}
