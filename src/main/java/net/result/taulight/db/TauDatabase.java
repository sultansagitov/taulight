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

    boolean saveMessage(MessageEntity msg) throws DatabaseException;

    List<ChatMessageViewDTO> loadMessages(ChatEntity chat, int index, int size) throws DatabaseException;

    Optional<MessageEntity> findMessage(UUID id) throws DatabaseException;

    boolean addMemberToChannel(ChannelEntity channel, MemberEntity member) throws DatabaseException;

    long getMessageCount(ChatEntity chat) throws DatabaseException;

    boolean leaveFromChannel(ChannelEntity channel, MemberEntity member) throws DatabaseException;

    void saveInviteCode(InviteCodeEntity code) throws DatabaseException;

    Optional<InviteCodeEntity> getInviteCode(String code) throws DatabaseException;

    boolean activateInviteCode(InviteCodeEntity code) throws DatabaseException;

    boolean saveReactionType(ReactionTypeEntity reaction) throws DatabaseException;

    boolean saveReactionEntry(ReactionEntryEntity reaction) throws DatabaseException;

    boolean removeReactionEntry(ReactionEntryEntity reaction) throws DatabaseException;

    List<ReactionTypeEntity> getReactionTypesByPackage(String packageName) throws DatabaseException;

    Collection<MemberEntity> getMembers(ChatEntity chat);
}
