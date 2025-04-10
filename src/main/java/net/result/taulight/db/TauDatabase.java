package net.result.taulight.db;

import net.result.sandnode.db.Database;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.exception.DatabaseException;
import net.result.taulight.exception.AlreadyExistingRecordException;

import java.util.*;

/**
 * Interface defining database operations specific to the TauLight module.
 * Extends the generic {@link net.result.sandnode.db.Database} interface.
 */
public interface TauDatabase extends Database {

    /**
     * Creates a dialog between two members.
     *
     * @param firstMember the first member
     * @param secondMember the second member
     * @return the created DialogEntity
     * @throws DatabaseException if a database error occurs
     * @throws AlreadyExistingRecordException if a dialog between the members already exists
     */
    DialogEntity createDialog(MemberEntity firstMember, MemberEntity secondMember)
            throws DatabaseException, AlreadyExistingRecordException;

    /**
     * Finds a dialog between two members.
     *
     * @param member1 the first member
     * @param member2 the second member
     * @return an optional DialogEntity if found
     * @throws DatabaseException if a database error occurs
     */
    Optional<DialogEntity> findDialog(MemberEntity member1, MemberEntity member2) throws DatabaseException;

    /**
     * Saves a chat entity to the database.
     *
     * @param chat the chat to save
     * @throws DatabaseException if a database error occurs
     * @throws AlreadyExistingRecordException if a chat with the same ID already exists
     */
    void saveChat(ChatEntity chat) throws DatabaseException, AlreadyExistingRecordException;

    /**
     * Retrieves a chat by its ID.
     *
     * @param id the UUID of the chat
     * @return an optional ChatEntity if found
     * @throws DatabaseException if a database error occurs
     */
    Optional<ChatEntity> getChat(UUID id) throws DatabaseException;

    /**
     * Saves a message to the database.
     *
     * @param msg the message to save
     * @throws DatabaseException if a database error occurs
     */
    void saveMessage(MessageEntity msg) throws DatabaseException;

    /**
     * Loads messages from a chat with pagination support.
     *
     * @param chat  the chat from which to load messages
     * @param index the starting index
     * @param size  the number of messages to load
     * @return a list of chat messages
     * @throws DatabaseException if a database error occurs
     */
    List<MessageEntity> loadMessages(ChatEntity chat, int index, int size) throws DatabaseException;

    /**
     * Finds a message by its ID.
     *
     * @param id the UUID of the message
     * @return an optional MessageEntity if found
     * @throws DatabaseException if a database error occurs
     */
    Optional<MessageEntity> findMessage(UUID id) throws DatabaseException;

    /**
     * Adds a member to a channel.
     *
     * @param channel the channel to join
     * @param member the member to add
     * @return {@code true} if the member was successfully added,
     *      {@code false} if the member was already part of the channel
     * @throws DatabaseException if a database error occurs
     */
    boolean addMemberToChannel(ChannelEntity channel, MemberEntity member) throws DatabaseException;

    /**
     * Gets the count of messages in a chat.
     *
     * @param chat the chat to count messages from
     * @return the total number of messages
     * @throws DatabaseException if a database error occurs
     */
    long getMessageCount(ChatEntity chat) throws DatabaseException;

    /**
     * Removes a member from a channel.
     *
     * @param channel the channel to leave
     * @param member the member to remove
     * @return {@code true} if the member was successfully removed,
     *      {@code false} if the member was not part of the channel
     * @throws DatabaseException if a database error occurs
     */
    boolean leaveFromChannel(ChannelEntity channel, MemberEntity member) throws DatabaseException;

    /**
     * Saves an invite code to the database.
     *
     * @param code the invite code to save
     * @throws DatabaseException if a database error occurs
     */
    void saveInviteCode(InviteCodeEntity code) throws DatabaseException;

    /**
     * Retrieves an invite code by its code string.
     *
     * @param code the code string
     * @return an optional InviteCodeEntity if found
     * @throws DatabaseException if a database error occurs
     */
    Optional<InviteCodeEntity> getInviteCode(String code) throws DatabaseException;

    /**
     * Activates a given invite code.
     *
     * @param code the invite code to activate
     * @return {@code true} if the code was valid and successfully activated,
     *      {@code false} if the code was already used or invalid
     * @throws DatabaseException if a database error occurs
     */
    boolean activateInviteCode(InviteCodeEntity code) throws DatabaseException;

    /**
     * Saves a reaction type to the database.
     *
     * @param reaction the reaction type to save
     * @throws DatabaseException if a database error occurs
     */
    void saveReactionType(ReactionTypeEntity reaction) throws DatabaseException;

    /**
     * Saves a reaction entry to the database.
     *
     * @param reaction the reaction entry to save
     * @throws DatabaseException if a database error occurs
     */
    void saveReactionEntry(ReactionEntryEntity reaction) throws DatabaseException;

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
    Collection<MemberEntity> getMembers(ChatEntity chat);

    /**
     * Removes a reaction entry from the database based on message, member, and reaction type.
     *
     * @param message the message the reaction is associated with
     * @param member the member who reacted
     * @param reactionType the type of reaction
     * @return {@code true} if the entry was successfully removed, {@code false} if the entry did not exist
     * @throws DatabaseException if a database error occurs
     */
    boolean removeReactionEntry(MessageEntity message, MemberEntity member, ReactionTypeEntity reactionType)
            throws DatabaseException;
}
