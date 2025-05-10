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
     * Creates a new reaction package with the specified name and description.
     *
     * @param packageName the name of the reaction package to create
     * @param description a brief description of the reaction package
     * @return the created {@link ReactionPackageEntity}
     * @throws DatabaseException if a database error occurs
     */
    ReactionPackageEntity createReactionPackage(String packageName, String description) throws DatabaseException;

    /**
     * Finds a {@link ReactionPackageEntity} by its name.
     *
     * @param packageName the name of the reaction package to find
     * @return an {@link Optional} containing the found {@link ReactionPackageEntity}, or empty if not found
     * @throws DatabaseException if a database error occurs during the lookup
     */
    Optional<ReactionPackageEntity> findReactionPackage(String packageName) throws DatabaseException;

    /**
     * Creates a new reaction type with the specified name and associates it with
     * the given {@link ReactionPackageEntity}.
     *
     * @param name the name of the reaction type to create
     * @param reactionPackage the {@link ReactionPackageEntity} to associate with
     * @return the created {@link ReactionTypeEntity}
     * @throws DatabaseException if a database error occurs
     */
    ReactionTypeEntity createReactionType(String name, ReactionPackageEntity reactionPackage) throws DatabaseException;

    /**
     * Creates multiple reaction types with the specified names and associates them with
     * the given {@link ReactionPackageEntity}.
     *
     * @param rp the {@link ReactionPackageEntity} to associate the new reaction types with
     * @param types a collection of reaction type names to create
     * @return a collection of created {@link ReactionTypeEntity} instances
     * @throws DatabaseException if a database error occurs during creation
     */
    Collection<ReactionTypeEntity> createReactionType(ReactionPackageEntity rp, Collection<String> types)
            throws DatabaseException;

    /**
     * Creates a new reaction entry by a member on a message using a specific reaction type.
     *
     * @param member the {@link TauMemberEntity} who is reacting
     * @param message the {@link MessageEntity} being reacted to
     * @param reactionType the {@link ReactionTypeEntity} representing the type of reaction
     * @return the created {@link ReactionEntryEntity}
     * @throws DatabaseException if a database error occurs
     */
    ReactionEntryEntity createReactionEntry(
            TauMemberEntity member,
            MessageEntity message,
            ReactionTypeEntity reactionType
    ) throws DatabaseException;

    /**
     * Removes a reaction entry from the database.
     *
     * @param reaction the reaction entry to remove
     * @return {@code true} if the entry was successfully removed, {@code false} if the entry did not exist
     * @throws DatabaseException if a database error occurs
     */
    boolean removeReactionEntry(ReactionEntryEntity reaction) throws DatabaseException;

    /**
     * Retrieves all reaction types associated with a specific package name.
     *
     * @param packageName the name of the reaction package
     * @return a list of ReactionTypeEntities
     * @throws DatabaseException if a database error occurs
     */
    List<ReactionTypeEntity> getReactionTypesByPackage(String packageName) throws DatabaseException;

    /**
     * Retrieves all members in a given chat.
     *
     * @param chat the chat whose members to retrieve
     * @return a collection of members in the chat
     */
    Collection<TauMemberEntity> getMembers(ChatEntity chat);

    /**
     * Removes a reaction entry from the database based on message, member, and reaction type.
     *
     * @param message the message the reaction is associated with
     * @param member the member who reacted
     * @param reactionType the type of reaction
     * @return {@code true} if the entry was successfully removed, {@code false} if the entry did not exist
     * @throws DatabaseException if a database error occurs
     */
    boolean removeReactionEntry(MessageEntity message, TauMemberEntity member, ReactionTypeEntity reactionType)
            throws DatabaseException;

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
