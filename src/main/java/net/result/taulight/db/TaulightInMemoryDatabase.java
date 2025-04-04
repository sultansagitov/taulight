package net.result.taulight.db;

import net.result.sandnode.db.Member;
import net.result.sandnode.db.InMemoryDatabase;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.security.PasswordHasher;
import net.result.taulight.exception.AlreadyExistingRecordException;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class TaulightInMemoryDatabase extends InMemoryDatabase implements TauDatabase {
    
    private final Map<UUID, TauDialog> dialogs = new HashMap<>();
    private final Map<UUID, TauChat> chats = new HashMap<>();
    private final Map<UUID, List<ServerChatMessage>> messages = new HashMap<>();
    private final Map<UUID, Set<Member>> chatMembers = new HashMap<>();
    private final Map<String, InviteCodeObject> inviteCodes = new HashMap<>();
    
    public TaulightInMemoryDatabase(PasswordHasher hasher) {
        super(hasher);
    }

    @Override
    public TauDialog createDialog(Member member1, Member member2) throws DatabaseException {
        // Check if dialog already exists
        Optional<TauDialog> existingDialog = findDialog(member1, member2);
        if (existingDialog.isPresent()) {
            return existingDialog.get();
        }
        
        // Create new dialog
        TauDialog dialog = new TauDialog(this, member1, member2);
        dialogs.put(dialog.id(), dialog);
        
        // Initialize members in chat
        Set<Member> members = new HashSet<>();
        members.add(member1);
        members.add(member2);
        chatMembers.put(dialog.id(), members);
        
        // Initialize message list
        messages.put(dialog.id(), new ArrayList<>());
        
        // Add to chats
        chats.put(dialog.id(), dialog);
        
        return dialog;
    }

    @Override
    public Optional<TauDialog> findDialog(Member member1, Member member2) throws DatabaseException {
        return dialogs.values().stream()
                .filter(dialog -> (dialog.firstMember().equals(member1) && dialog.secondMember().equals(member2)) ||
                                 (dialog.firstMember().equals(member2) && dialog.secondMember().equals(member1)))
                .findFirst();
    }

    @Override
    public void saveChat(TauChat chat) throws DatabaseException, AlreadyExistingRecordException {
        if (chats.containsKey(chat.id())) {
            throw new AlreadyExistingRecordException("Chat with ID " + chat.id() + " already exists");
        }
        chats.put(chat.id(), chat);
        
        // Initialize message list and members set if not already present
        if (!messages.containsKey(chat.id())) {
            messages.put(chat.id(), new ArrayList<>());
        }
        
        if (!chatMembers.containsKey(chat.id())) {
            chatMembers.put(chat.id(), new HashSet<>());
        }
    }

    @Override
    public Optional<TauChat> getChat(UUID id) throws DatabaseException {
        return Optional.ofNullable(chats.get(id));
    }

    @Override
    public void saveMessage(ServerChatMessage msg) throws DatabaseException, AlreadyExistingRecordException {
        UUID chatId = msg.message().chatID();
        
        if (!messages.containsKey(chatId)) {
            messages.put(chatId, new ArrayList<>());
        }
        
        List<ServerChatMessage> chatMessages = messages.get(chatId);
        
        // Check if message with same ID already exists
        if (chatMessages.stream().anyMatch(m -> m.equals(msg))) {
            throw new AlreadyExistingRecordException("Message", "ID", msg.id());
        }
        
        chatMessages.add(msg);
        // Sort messages by timestamp to maintain order
        chatMessages.sort(Comparator.comparing(ServerChatMessage::getCreationDate));
    }

    @Override
    public List<ServerChatMessage> loadMessages(TauChat chat, int index, int size) throws DatabaseException {
        List<ServerChatMessage> chatMessages = messages.getOrDefault(chat.id(), new ArrayList<>());
        
        // Calculate start and end indices
        int start = Math.max(0, Math.min(index, chatMessages.size()));
        int end = Math.min(chatMessages.size(), start + size);
        
        return new ArrayList<>(chatMessages.subList(start, end));
    }

    @Override
    public Collection<Member> getMembersFromChannel(TauChannel channel) throws DatabaseException {
        return chatMembers.getOrDefault(channel.id(), new HashSet<>());
    }

    @Override
    public void addMemberToChat(TauChat chat, Member member) throws DatabaseException {
        UUID chatId = chat.id();
        
        if (!chatMembers.containsKey(chatId)) {
            chatMembers.put(chatId, new HashSet<>());
        }
        
        chatMembers.get(chatId).add(member);
    }

    @Override
    public Collection<TauChat> getChats(Member member) throws DatabaseException {
        return chats.values().stream()
                .filter(chat -> chatMembers.getOrDefault(chat.id(), new HashSet<>()).contains(member))
                .collect(Collectors.toList());
    }

    @Override
    public void removeChat(UUID chatID) throws DatabaseException {
        chats.remove(chatID);
        messages.remove(chatID);
        chatMembers.remove(chatID);
        dialogs.values().removeIf(dialog -> dialog.id().equals(chatID));
    }

    @Override
    public long getMessageCount(TauChat chat) throws DatabaseException {
        return messages.getOrDefault(chat.id(), new ArrayList<>()).size();
    }

    @Override
    public void leaveFromChat(TauChat chat, Member member) throws DatabaseException {
        UUID chatId = chat.id();
        
        if (chatMembers.containsKey(chatId)) {
            chatMembers.get(chatId).remove(member);
            
            // If no members left, remove the chat
            if (chatMembers.get(chatId).isEmpty()) {
                removeChat(chatId);
            }
        }
    }

    @Override
    public void createInviteCode(InviteCodeObject code) throws DatabaseException, AlreadyExistingRecordException {
        String codeValue = code.getCode();
        
        if (inviteCodes.containsKey(codeValue)) {
            throw new AlreadyExistingRecordException("Invite code " + codeValue + " already exists");
        }
        
        inviteCodes.put(codeValue, code);
    }

    @Override
    public Optional<InviteCodeObject> getInviteCode(String code) throws DatabaseException {
        return Optional.ofNullable(inviteCodes.get(code));
    }

    @Override
    public boolean activateInviteCode(InviteCodeObject code) throws DatabaseException {
        String codeValue = code.getCode();
        
        if (inviteCodes.containsKey(codeValue)) {
            inviteCodes.get(codeValue).setActivationDateNow();
            return true;
        }
        
        return false;
    }

    @Override
    public boolean deleteInviteCode(String code) throws DatabaseException {
        return inviteCodes.remove(code) != null;
    }

    @Override
    public List<InviteCodeObject> getInviteCodesBySender(String senderNickname, boolean includeExpired, boolean includeActivated) throws DatabaseException {
        return inviteCodes.values().stream()
                .filter(code -> code.getSenderNickname().equals(senderNickname))
                .filter(code -> includeExpired || (code.getExpiresData() == null || !ZonedDateTime.now().isAfter(code.getExpiresData())))
                .filter(code -> includeActivated || (code.getActivationDate() == null))
                .collect(Collectors.toList());
    }

    @Override
    public List<InviteCodeObject> getActiveInviteCodes(TauChannel channel) throws DatabaseException {
        return inviteCodes.values().stream()
                .filter(code -> code.getExpiresData() == null || !ZonedDateTime.now().isAfter(code.getExpiresData()))
                .filter(code -> code.getActivationDate() == null)
                .filter(code -> code.getChatID().equals(channel.id()))
                .collect(Collectors.toList());
    }

    @Override
    public List<InviteCodeObject> getInviteCodesByNickname(Member member) throws DatabaseException {
        return inviteCodes.values().stream()
                .filter(code -> code.getNickname().equals(member.nickname()))
                .collect(Collectors.toList());
    }

    @Override
    public int countActiveInvitesByNickname(String nickname) throws DatabaseException {
        return (int) inviteCodes.values().stream()
                .filter(code -> code.getNickname().equals(nickname))
                .filter(code -> code.getExpiresData() == null || !ZonedDateTime.now().isAfter(code.getExpiresData()))
                .filter(code -> code.getActivationDate() == null)
                .count();
    }
}
