package net.result.taulight;

import net.result.sandnode.db.Member;
import net.result.taulight.messenger.TauChat;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

public class TauChatManager {
    public final Collection<TauChat> chats = new HashSet<>();

    public Collection<TauChat> getChats(Member member) {
        return chats.stream()
                .filter(chat -> chat.getMembers().contains(member))
                .collect(Collectors.toSet());
    }

    public void addNew(TauChat chat) {
        chats.add(chat);
    }

    public Optional<TauChat> find(String groupName) {
        return chats.stream()
                .filter(chat -> chat.getID().equals(groupName))
                .findFirst();
    }

    public void addMember(TauChat chat, Member member) {
        chat.getMembers().add(member);
    }
}
