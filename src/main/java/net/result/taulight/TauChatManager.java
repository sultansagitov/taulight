package net.result.taulight;

import net.result.sandnode.db.Member;
import net.result.taulight.messenger.TauChat;

import java.util.Collection;
import java.util.Optional;

public interface TauChatManager {
    Collection<TauChat> getChats(Member member);

    void addNew(TauChat chat);

    Optional<TauChat> find(String chatID);

    void addMember(TauChat chat, Member member);

    Optional<TauChat> get(String id);
}
