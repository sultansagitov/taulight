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

    void createInviteToken(InviteCodeObject inviteCode) throws DatabaseException, AlreadyExistingRecordException;

    Optional<InviteCodeObject> getInviteToken(String code) throws DatabaseException;

    boolean activateInviteToken(InviteCodeObject token) throws DatabaseException;

    boolean deleteInviteToken(String code) throws DatabaseException;

    List<InviteCodeObject> getInviteTokensBySender(
            String senderNickname,
            boolean includeExpired,
            boolean includeActivated
    ) throws DatabaseException;

    List<InviteCodeObject> getActiveInviteCode(TauChannel channel) throws DatabaseException;

    List<InviteCodeObject> getInviteCodesByNickname(String nickname) throws DatabaseException;

    int countActiveInvitesByNickname(String nickname) throws DatabaseException;
}
