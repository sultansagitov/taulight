package net.result.taulight.db;

import net.result.sandnode.db.Database;
import net.result.sandnode.db.Member;

import java.util.Collection;
import java.util.Optional;

public interface TauDatabase extends Database {

    TauChat createDirectChat(Member member1, Member member2);

    Optional<TauDirect> findDirectChat(Member member1, Member member2);

    void saveChat(TauChat chat);

    Optional<TauChat> getChat(String id);

    void saveMessage(ChatMessage msg);

    Collection<ChatMessage> loadMessages(TauChat chat, int index, int size);

    Collection<Member> getMembersFromChat(TauChat chat);

    void addMemberToChat(TauChat chat, Member member);

    Collection<TauChat> getChats(Member member);

    void removeChat(String chatId);

    long getMessageCount(TauChat chat);

}
