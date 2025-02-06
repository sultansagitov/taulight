package net.result.taulight.db;

import net.result.sandnode.db.Database;
import net.result.sandnode.db.Member;
import net.result.taulight.messenger.TauChat;

import java.util.Collection;
import java.util.Optional;

public interface TauDatabase extends Database {

    void saveChat(TauChat chat);

    Optional<TauChat> getChat(String id);

    void saveMessage(ChatMessage msg);

    Collection<ChatMessage> loadMessages(TauChat chat, int index, int size);

    Collection<Member> getMembersFromChat(TauChat chat);

    void addMemberToChat(TauChat chat, Member member);

    Collection<TauChat> getChats(Member member);
}
