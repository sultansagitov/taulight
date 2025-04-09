package net.result.taulight.db;

import net.result.sandnode.db.JPADatabase;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.security.PasswordHasher;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.exception.AlreadyExistingRecordException;

import java.util.*;
import java.util.stream.Collectors;

public class TauJPADatabase extends JPADatabase implements TauDatabase {
    private final ChannelRepository channelRepository;
    private final DialogRepository dialogRepository;
    private final MessageRepository messageRepository;
    private final InviteCodeRepository inviteCodeRepository;
    private final ReactionTypeRepository reactionTypeRepository;
    private final ReactionEntryRepository reactionEntryRepository;

    public TauJPADatabase(PasswordHasher hasher) {
        super(hasher);
        this.channelRepository = new ChannelRepository();
        this.dialogRepository = new DialogRepository();
        this.messageRepository = new MessageRepository();
        this.inviteCodeRepository = new InviteCodeRepository();
        this.reactionTypeRepository = new ReactionTypeRepository();
        this.reactionEntryRepository = new ReactionEntryRepository();
    }

    @Override
    public DialogEntity createDialog(MemberEntity firstMember, MemberEntity secondMember) throws AlreadyExistingRecordException, DatabaseException {
        DialogEntity dialog = new DialogEntity(firstMember, secondMember);
        dialogRepository.save(dialog);
        return dialog;
    }

    @Override
    public Optional<DialogEntity> findDialog(MemberEntity firstMember, MemberEntity secondMember) {
        return dialogRepository.findByMembers(firstMember, secondMember);
    }

    @Override
    public boolean saveChat(ChatEntity chat) throws AlreadyExistingRecordException, DatabaseException {
        if (chat instanceof ChannelEntity) {
            channelRepository.save((ChannelEntity) chat);
            return true;
        }

        if (chat instanceof DialogEntity) {
            dialogRepository.save((DialogEntity) chat);
            return true;
        }

        throw new IllegalArgumentException("Unknown chat type");
    }

    @Override
    public Optional<ChatEntity> getChat(UUID id) throws DatabaseException {
        Optional<ChannelEntity> channel = channelRepository.findById(id);
        if (channel.isPresent()) return channel.map(c -> c);
        return dialogRepository.findById(id).map(d -> d);
    }

    @Override
    public boolean saveMessage(MessageEntity msg) throws AlreadyExistingRecordException {
        messageRepository.save(msg);
        return true;
    }

    @Override
    public List<ChatMessageViewDTO> loadMessages(ChatEntity chat, int index, int size) throws DatabaseException {
        return messageRepository.findMessagesByChat(chat, index, size);
    }

    @Override
    public Optional<MessageEntity> findMessage(UUID id) throws DatabaseException {
        return messageRepository.findById(id);
    }

    @Override
    public Collection<MemberEntity> getMembersFromChannel(ChannelEntity channel) throws DatabaseException {
        return channelRepository.findMembersByChannel(channel);
    }

    @Override
    public boolean addMemberToChat(ChatEntity chat, MemberEntity member) throws DatabaseException {
        if (chat instanceof ChannelEntity) {
            channelRepository.addMemberToChannel((ChannelEntity) chat, member);
        } else if (chat instanceof DialogEntity) {
            dialogRepository.addMemberToDialog((DialogEntity) chat, member);
        } else {
            throw new IllegalArgumentException("Unknown chat type");
        }
        return true;
    }

    @Override
    public Collection<ChatEntity> getChats(MemberEntity member) {
        Set<ChatEntity> objectStream = member.channels().stream().map(c -> (ChatEntity) c).collect(Collectors.toSet());
        objectStream.addAll(member.dialogs());
        return objectStream;
    }

    @Override
    public long getMessageCount(ChatEntity chat) throws DatabaseException {
        return messageRepository.countMessagesByChat(chat);
    }

    @Override
    public boolean leaveFromChat(ChatEntity chat, MemberEntity member) throws DatabaseException {
        if (chat instanceof ChannelEntity channel) {
            channelRepository.removeMemberFromChannel(channel, member);
        } else if (chat instanceof DialogEntity dialog) {
            dialogRepository.removeMemberFromDialog(dialog, member);
        } else {
            throw new IllegalArgumentException("Unknown chat type");
        }
        return true;
    }

    @Override
    public boolean createInviteCode(InviteCodeEntity code) throws AlreadyExistingRecordException, DatabaseException {
        inviteCodeRepository.save(code);
        return true;
    }

    @Override
    public Optional<InviteCodeEntity> getInviteCode(String code) throws DatabaseException {
        return inviteCodeRepository.findByCode(code);
    }

    @Override
    public boolean activateInviteCode(InviteCodeEntity code) throws DatabaseException {
        inviteCodeRepository.activate(code);
        return true;
    }

    @Override
    public boolean deleteInviteCode(String code) throws DatabaseException {
        Optional<InviteCodeEntity> inviteCode = inviteCodeRepository.findByCode(code);
        if (inviteCode.isPresent()) {
            inviteCodeRepository.delete(inviteCode.get());
        }
        return inviteCode.isPresent();
    }

    @Override
    public List<InviteCodeEntity> getInviteCodesBySender(
            MemberEntity sender,
            boolean includeExpired,
            boolean includeActivated
    ) throws DatabaseException {
        return inviteCodeRepository.findBySender(sender, includeExpired, includeActivated);
    }

    @Override
    public List<InviteCodeEntity> getActiveInviteCodes(ChannelEntity channel) {
        return inviteCodeRepository.ByChannel(channel);
    }

    @Override
    public List<InviteCodeEntity> getInviteCodesByNickname(MemberEntity member) {
        return inviteCodeRepository.findByNickname(member);
    }

    @Override
    public int countActiveInvitesByNickname(MemberEntity member) {
        return inviteCodeRepository.countActiveByNickname(member);
    }

    @Override
    public boolean saveReactionType(ReactionTypeEntity reaction) throws AlreadyExistingRecordException {
        reactionTypeRepository.save(reaction);
        return true;
    }

    @Override
    public boolean removeReactionType(ReactionTypeEntity reaction) {
        reactionTypeRepository.delete(reaction);
        return true;
    }

    @Override
    public boolean saveReactionEntry(ReactionEntry reaction) throws AlreadyExistingRecordException {
        reactionEntryRepository.save(reaction);
        return true;
    }

    @Override
    public boolean removeReactionEntry(ReactionEntry reaction) {
        reactionEntryRepository.delete(reaction);
        return true;
    }

    @Override
    public Optional<ReactionTypeEntity> getReactionTypeByName(String name) {
        return reactionTypeRepository.findByName(name);
    }

    @Override
    public List<ReactionTypeEntity> getReactionTypesByPackage(String packageName) {
        return reactionTypeRepository.findByPackageName(packageName);
    }

    @Override
    public List<ReactionEntry> getReactionsByMessage(ChatMessageViewDTO message) {
        return reactionEntryRepository.findByMessage(message);
    }

    @Override
    public Collection<MemberEntity> getMembers(ChatEntity chat) {
        if (chat instanceof ChannelEntity) {
            return channelRepository.findMembersByChannel((ChannelEntity) chat);
        } else if (chat instanceof DialogEntity) {
            return dialogRepository.findMembersByDialog((DialogEntity) chat);
        }
        return List.of();
    }
}