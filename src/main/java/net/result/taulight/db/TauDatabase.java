package net.result.taulight.db;

import net.result.sandnode.db.Database;
import net.result.sandnode.db.Member;
import net.result.sandnode.exception.DatabaseException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TauDatabase extends Database {

    TauDialog createDialog(Member member1, Member member2) throws DatabaseException;

    Optional<TauDialog> findDialog(Member member1, Member member2) throws DatabaseException;

    void saveChat(TauChat chat) throws DatabaseException;

    Optional<TauChat> getChat(UUID id) throws DatabaseException;

    void saveMessage(ServerChatMessage msg) throws DatabaseException;

    List<ServerChatMessage> loadMessages(TauChat chat, int index, int size) throws DatabaseException;

    Collection<Member> getMembersFromChat(TauChat chat) throws DatabaseException;

    void addMemberToChat(TauChat chat, Member member) throws DatabaseException;

    Collection<TauChat> getChats(Member member) throws DatabaseException;

    void removeChat(UUID chatID) throws DatabaseException;

    long getMessageCount(TauChat chat) throws DatabaseException;

    void leaveFromChat(TauChannel channel, Member member) throws DatabaseException;
}
