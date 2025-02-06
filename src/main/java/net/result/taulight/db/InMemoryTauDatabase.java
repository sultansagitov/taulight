package net.result.taulight.db;

import net.result.sandnode.db.InMemoryDatabase;
import net.result.sandnode.db.Member;
import net.result.taulight.messenger.TauChat;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTauDatabase extends InMemoryDatabase implements TauDatabase {
    private final Set<TauChat> chats = Collections.synchronizedSet(new HashSet<>());
    private final Set<ChatMessage> messages = Collections.synchronizedSet(new HashSet<>());
    private final Map<String, Set<Member>> chatMembers = new HashMap<>();

    @Override
    public void saveChat(TauChat chat) {
        if (chat == null) {
            throw new IllegalArgumentException("Chat cannot be null");
        }

        chats.removeIf(existingChat -> existingChat.getID().equals(chat.getID()));
        chats.add(chat);

        chatMembers.putIfAbsent(chat.getID(), Collections.synchronizedSet(new HashSet<>()));
    }

    @Override
    public Optional<TauChat> getChat(String id) {
        if (id == null) {
            return Optional.empty();
        }

        return chats.stream()
                .filter(chat -> chat.getID().equals(id))
                .findFirst();
    }

    @Override
    public void saveMessage(ChatMessage msg) {
        if (msg == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }

        if (getChat(msg.chatID()).isEmpty()) {
            throw new IllegalStateException("Cannot save message for non-existent chat");
        }

        messages.add(msg);
    }

    @Override
    public Collection<ChatMessage> loadMessages(TauChat chat, int index, int size) {
        if (chat == null) {
            return Collections.emptyList();
        }

        return messages.stream()
                .filter(chatMessage -> chatMessage.chatID().equals(chat.getID()))
                .sorted(Comparator.comparing(ChatMessage::ztd).reversed())
                .skip(Math.max(0, index))
                .limit(Math.max(0, size))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Member> getMembersFromChat(TauChat chat) {
        if (chat == null) {
            return Collections.emptyList();
        }

        return Optional.ofNullable(chatMembers.get(chat.getID()))
                .map(c -> (Collection<Member>) new HashSet<>(c))
                .orElse(Collections.emptySet());
    }

    @Override
    public void addMemberToChat(TauChat chat, Member member) {
        if (chat == null || member == null) {
            throw new IllegalArgumentException("Chat and member cannot be null");
        }

        if (getChat(chat.getID()).isEmpty()) {
            throw new IllegalStateException("Cannot add member to non-existent chat");
        }

        Set<Member> members = chatMembers.computeIfAbsent(
                chat.getID(),
                k -> Collections.synchronizedSet(new HashSet<>())
        );

        members.add(member);
    }

    @Override
    public Collection<TauChat> getChats(Member member) {
        if (member == null) {
            return Collections.emptyList();
        }

        return chats.stream()
                .filter(chat -> getMembersFromChat(chat).contains(member))
                .collect(Collectors.toList());
    }

    @Override
    public void removeChat(String chatId) {
        chats.removeIf(chat -> chat.getID().equals(chatId));
        messages.removeIf(msg -> msg.chatID().equals(chatId));
        chatMembers.remove(chatId);
    }

    @Override
    public long getMessageCount(TauChat chat) {
        if (chat == null) {
            return 0;
        }

        return messages.stream()
                .filter(msg -> msg.chatID().equals(chat.getID()))
                .count();
    }
}