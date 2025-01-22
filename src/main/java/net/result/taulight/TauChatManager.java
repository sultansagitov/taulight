package net.result.taulight;

import net.result.sandnode.db.IMember;
import net.result.taulight.messenger.TauChat;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class TauChatManager {
    public final Set<TauChat> chats = new HashSet<>();

    public Set<TauChat> getChats(IMember member) {
        return chats.stream()
                .filter(chat -> chat.members.contains(member))
                .collect(Collectors.toSet());
    }

    public void addNew(TauChat chat) {
        chats.add(chat);
    }

    public Optional<TauChat> find(String groupName) {
        return chats.stream()
                .filter(chat -> chat.name.equals(groupName))
                .findFirst();
    }

    public void addMember(TauChat chat, IMember member) {
        chat.members.add(member);
    }
}
