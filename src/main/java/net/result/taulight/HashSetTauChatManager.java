package net.result.taulight;

import net.result.sandnode.db.Member;
import net.result.taulight.messenger.TauChat;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

public class HashSetTauChatManager implements TauChatManager {
    public final Collection<TauChat> chats = new HashSet<>();

    @Override
    public Collection<TauChat> getChats(Member member) {
        return chats.stream()
                .filter(chat -> chat.getMembers().contains(member))
                .collect(Collectors.toSet());
    }

    @Override
    public void addNew(TauChat chat) {
        chats.add(chat);
    }

    @Override
    public Optional<TauChat> find(String chatID) {
        return chats.stream().filter(chat -> chat.getID().equals(chatID)).findFirst();
    }

    @Override
    public void addMember(TauChat chat, Member member) {
        chat.getMembers().add(member);
        member.getSessions().forEach(session -> session.addToGroup(chat.group));
    }

    @Override
    public Optional<TauChat> get(String id) {
        return chats.stream().filter(chat -> chat.getID().equals(id)).findFirst();
    }
}
