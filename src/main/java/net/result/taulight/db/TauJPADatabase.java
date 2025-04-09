package net.result.taulight.db;

import net.result.sandnode.db.JPADatabase;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.security.PasswordHasher;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.exception.AlreadyExistingRecordException;

import java.util.*;

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
    public Optional<DialogEntity> findDialog(MemberEntity firstMember, MemberEntity secondMember)
            throws DatabaseException {
        return dialogRepository.findByMembers(firstMember, secondMember);
    }

    @Override
    public boolean saveChat(ChatEntity chat) throws DatabaseException, AlreadyExistingRecordException {
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
    public boolean saveMessage(MessageEntity msg) throws DatabaseException {
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
    public boolean addMemberToChannel(ChannelEntity channel, MemberEntity member) throws DatabaseException {
        return channelRepository.addMemberToChannel(channel, member);
    }

    @Override
    public long getMessageCount(ChatEntity chat) throws DatabaseException {
        return messageRepository.countMessagesByChat(chat);
    }

    @Override
    public boolean leaveFromChannel(ChannelEntity channel, MemberEntity member) throws DatabaseException {
        return channelRepository.removeMemberFromChannel(channel, member);
    }

    @Override
    public void saveInviteCode(InviteCodeEntity code) throws DatabaseException {
        inviteCodeRepository.save(code);
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
    public boolean saveReactionType(ReactionTypeEntity reactionType) throws DatabaseException {
        reactionTypeRepository.save(reactionType);
        return true;
    }

    @Override
    public boolean saveReactionEntry(ReactionEntryEntity reactionEntry) throws DatabaseException {
        reactionEntryRepository.save(reactionEntry);
        return true;
    }

    @Override
    public boolean removeReactionEntry(ReactionEntryEntity reaction) throws DatabaseException {
        reactionEntryRepository.delete(reaction);
        return true;
    }

    @Override
    public List<ReactionTypeEntity> getReactionTypesByPackage(String packageName) throws DatabaseException {
        return reactionTypeRepository.findByPackageName(packageName);
    }

    @Override
    public Collection<MemberEntity> getMembers(ChatEntity chat) {
        if (chat instanceof ChannelEntity channel) {
            return channel.members();
        } else if (chat instanceof DialogEntity dialog) {
            return Set.of(dialog.firstMember(), dialog.secondMember());
        }
        return List.of();
    }
}