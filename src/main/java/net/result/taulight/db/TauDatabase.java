package net.result.taulight.db;

import net.result.sandnode.db.Database;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.exception.AlreadyExistingRecordException;

import java.time.ZonedDateTime;
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
    DialogEntity createDialog(TauMemberEntity firstMember, TauMemberEntity secondMember)
            throws DatabaseException, AlreadyExistingRecordException;

    /**
     * Finds a dialog between two members.
     *
     * @param member1 the first member
     * @param member2 the second member
     * @return an optional DialogEntity if found
     * @throws DatabaseException if a database error occurs
     */
    Optional<DialogEntity> findDialog(TauMemberEntity member1, TauMemberEntity member2) throws DatabaseException;

    /**
     * Creates a new channel with the given title and owner. And adds owner to channel
     *
     * @param title the title of the new channel
     * @param owner the {@link TauMemberEntity} who will own the channel
     * @return the created {@link ChannelEntity}
     * @throws DatabaseException if a database error occurs
     */
    ChannelEntity createChannel(String title, TauMemberEntity owner) throws DatabaseException;

    /**
     * Retrieves a chat by its ID.
     *
     * @param id the UUID of the chat
     * @return an optional ChatEntity if found
     * @throws DatabaseException if a database error occurs
     */
    Optional<ChatEntity> getChat(UUID id) throws DatabaseException;

    /**
     * Creates a new message in the specified chat.
     *
     * @param chat the {@link ChatEntity} where the message will be added
     * @param input the {@link ChatMessageInputDTO} containing the message content and metadata
     * @param member the {@link TauMemberEntity} authoring the message
     * @return the created {@link MessageEntity}
     * @throws NotFoundException if the chat or member is not found
     * @throws DatabaseException if a database error occurs
     */
    MessageEntity createMessage(ChatEntity chat, ChatMessageInputDTO input, TauMemberEntity member)
            throws NotFoundException, DatabaseException;

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
    boolean addMemberToChannel(ChannelEntity channel, TauMemberEntity member) throws DatabaseException;

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
    boolean leaveFromChannel(ChannelEntity channel, TauMemberEntity member) throws DatabaseException;

    /**
     * Retrieves an invite code by its code string.
     *
     * @param code the code string
     * @return an optional InviteCodeEntity if found
     * @throws DatabaseException if a database error occurs
     */
    Optional<InviteCodeEntity> findInviteCode(String code) throws DatabaseException;

    /**
     * Creates a new invite code for a specific channel and receiver.
     *
     * @param channel the {@link ChannelEntity} to which the invite code grants access
     * @param receiver the {@link TauMemberEntity} who is intended to receive the invite
     * @param sender the {@link TauMemberEntity} who is sending the invite
     * @param expiresDate the expiration date and time of the invite code
     * @return the created {@link InviteCodeEntity}
     * @throws DatabaseException if a database error occurs
     */
    InviteCodeEntity createInviteCode(
            ChannelEntity channel,
            TauMemberEntity receiver,
            TauMemberEntity sender,
            ZonedDateTime expiresDate
    ) throws DatabaseException;

    /**
     * Retrieves all invite codes associated with a specific channel and member.
     *
     * @param channel the {@link ChannelEntity} to filter invite codes
     * @param member the {@link MemberEntity} who created or is associated with the codes
     * @return a collection of {@link InviteCodeEntity} instances matching the criteria
     * @throws DatabaseException if a database error occurs
     */
    Collection<InviteCodeEntity> findInviteCode(ChannelEntity channel, TauMemberEntity member) throws DatabaseException;

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

    void setAvatarForChannel(ChannelEntity channel, String contentType, String filename) throws DatabaseException;
}
