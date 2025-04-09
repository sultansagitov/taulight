package net.result.taulight.db;

import net.result.sandnode.db.Database;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.exception.DatabaseException;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.exception.AlreadyExistingRecordException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TauDatabase extends Database {

    DialogEntity createDialog(MemberEntity firstMember, MemberEntity secondMember) throws DatabaseException, AlreadyExistingRecordException;

    Optional<DialogEntity> findDialog(MemberEntity member1, MemberEntity member2) throws DatabaseException;

    boolean saveChat(ChatEntity chat) throws DatabaseException, AlreadyExistingRecordException;

    Optional<ChatEntity> getChat(UUID id) throws DatabaseException;

    boolean saveMessage(MessageEntity msg) throws DatabaseException, AlreadyExistingRecordException;

    List<ChatMessageViewDTO> loadMessages(ChatEntity chat, int index, int size) throws DatabaseException;

    Optional<MessageEntity> findMessage(UUID id) throws DatabaseException;

    Collection<MemberEntity> getMembersFromChannel(ChannelEntity channel) throws DatabaseException;

    boolean addMemberToChat(ChatEntity chat, MemberEntity member) throws DatabaseException;

    Collection<ChatEntity> getChats(MemberEntity member) throws DatabaseException;

    long getMessageCount(ChatEntity chat) throws DatabaseException;

    boolean leaveFromChat(ChatEntity chat, MemberEntity member) throws DatabaseException;

    boolean createInviteCode(InviteCodeEntity code) throws DatabaseException, AlreadyExistingRecordException;

    Optional<InviteCodeEntity> getInviteCode(String code) throws DatabaseException;

    boolean activateInviteCode(InviteCodeEntity code) throws DatabaseException;

    boolean deleteInviteCode(String code) throws DatabaseException;

    List<InviteCodeEntity> getInviteCodesBySender(
            MemberEntity sender,
            boolean includeExpired,
            boolean includeActivated
    ) throws DatabaseException;

    List<InviteCodeEntity> getActiveInviteCodes(ChannelEntity channel) throws DatabaseException;

    List<InviteCodeEntity> getInviteCodesByNickname(MemberEntity member) throws DatabaseException;

    int countActiveInvitesByNickname(MemberEntity member) throws DatabaseException;

    boolean saveReactionType(ReactionTypeEntity reaction) throws DatabaseException, AlreadyExistingRecordException;

    boolean removeReactionType(ReactionTypeEntity reaction) throws DatabaseException;

    boolean saveReactionEntry(ReactionEntry reaction) throws DatabaseException, AlreadyExistingRecordException;

    boolean removeReactionEntry(ReactionEntry reaction) throws DatabaseException;

    Optional<ReactionTypeEntity> getReactionTypeByName(String name) throws DatabaseException;

    List<ReactionTypeEntity> getReactionTypesByPackage(String packageName) throws DatabaseException;

    List<ReactionEntry> getReactionsByMessage(ChatMessageViewDTO message) throws DatabaseException;

    Collection<MemberEntity> getMembers(ChatEntity chat);
}
