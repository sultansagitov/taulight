package net.result.taulight.db;

import net.result.sandnode.db.Database;
import net.result.sandnode.db.Member;
import net.result.sandnode.exception.DatabaseException;
import net.result.taulight.exception.AlreadyExistingRecordException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TauDatabase extends Database {

    TauDialog createDialog(Member member1, Member member2) throws DatabaseException;

    Optional<TauDialog> findDialog(Member member1, Member member2) throws DatabaseException;

    void saveChat(TauChat chat) throws DatabaseException, AlreadyExistingRecordException;

    Optional<TauChat> getChat(UUID id) throws DatabaseException;

    void saveMessage(ServerChatMessage msg) throws DatabaseException, AlreadyExistingRecordException;

    List<ServerChatMessage> loadMessages(TauChat chat, int index, int size) throws DatabaseException;

    Collection<Member> getMembersFromChannel(TauChannel channel) throws DatabaseException;

    void addMemberToChat(TauChat chat, Member member) throws DatabaseException;

    Collection<TauChat> getChats(Member member) throws DatabaseException;

    void removeChat(UUID chatID) throws DatabaseException;

    long getMessageCount(TauChat chat) throws DatabaseException;

    void leaveFromChat(TauChat chat, Member member) throws DatabaseException;

    void createInviteToken(InviteToken inviteToken) throws DatabaseException, AlreadyExistingRecordException;

    Optional<InviteToken> getInviteToken(String rejectCode) throws DatabaseException;

    boolean deleteInviteToken(String rejectCode) throws DatabaseException;

    List<InviteToken> getActiveInviteToken(TauChannel channel) throws DatabaseException;

    void cleanupExpiredTokens() throws DatabaseException;

    List<InviteToken> getInviteLinksByNickname(String nickname) throws DatabaseException;

    int countActiveInvitesByNickname(String nickname) throws DatabaseException;
}
