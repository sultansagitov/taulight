package net.result.taulight.db;

import net.result.sandnode.db.JPADatabase;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.security.PasswordHasher;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.exception.AlreadyExistingRecordException;

import java.time.ZonedDateTime;
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
    public DialogEntity createDialog(MemberEntity firstMember, MemberEntity secondMember)
            throws AlreadyExistingRecordException, DatabaseException {
        return dialogRepository.save(new DialogEntity(firstMember, secondMember));
    }

    @Override
    public Optional<DialogEntity> findDialog(MemberEntity firstMember, MemberEntity secondMember)
            throws DatabaseException {
        return dialogRepository.findByMembers(firstMember, secondMember);
    }

    @Override
    public ChannelEntity createChannel(String title, MemberEntity owner) throws DatabaseException {
        return channelRepository.create(title, owner);
    }

    @Override
    public Optional<ChatEntity> getChat(UUID id) throws DatabaseException {
        Optional<ChannelEntity> channel = channelRepository.findById(id);
        if (channel.isPresent()) return channel.map(c -> c);
        return dialogRepository.findById(id).map(d -> d);
    }

    @Override
    public List<MessageEntity> loadMessages(ChatEntity chat, int index, int size) throws DatabaseException {
        return messageRepository.findMessagesByChat(chat, index, size);
    }

    @Override
    public MessageEntity createMessage(ChatEntity chat, ChatMessageInputDTO input, MemberEntity member)
            throws DatabaseException, NotFoundException {
        return messageRepository.create(chat, input, member);
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
    public InviteCodeEntity createInviteCode(
            ChannelEntity channel,
            MemberEntity receiver,
            MemberEntity sender,
            ZonedDateTime expiresDate
    ) throws DatabaseException {
        return inviteCodeRepository.create(channel, receiver, sender, expiresDate);
    }

    @Override
    public Optional<InviteCodeEntity> findInviteCode(String code) throws DatabaseException {
        return inviteCodeRepository.find(code);
    }

    @Override
    public Collection<InviteCodeEntity> findInviteCode(ChannelEntity channel, MemberEntity member)
            throws DatabaseException {
        return inviteCodeRepository.find(channel, member);
    }

    @Override
    public boolean activateInviteCode(InviteCodeEntity code) throws DatabaseException {
        return inviteCodeRepository.activate(code);
    }

    @Override
    public ReactionTypeEntity createReactionType(String name, String packageName) throws DatabaseException {
        return reactionTypeRepository.create(name, packageName);
    }

    @Override
    public ReactionEntryEntity createReactionEntry(
            MemberEntity member,
            MessageEntity message,
            ReactionTypeEntity reactionType
    ) throws DatabaseException {
        return reactionEntryRepository.create(member, message, reactionType);
    }

    @Override
    public boolean removeReactionEntry(ReactionEntryEntity reaction) throws DatabaseException {
        return reactionEntryRepository.delete(reaction);
    }

    @Override
    public List<ReactionTypeEntity> getReactionTypesByPackage(String packageName) throws DatabaseException {
        return reactionTypeRepository.findByPackageName(packageName);
    }

    @Override
    public Collection<MemberEntity> getMembers(ChatEntity chat) {
        if (chat instanceof ChannelEntity channel) return channel.members();
        if (chat instanceof DialogEntity dialog) return Set.of(dialog.firstMember(), dialog.secondMember());
        return Set.of();
    }

    @Override
    public boolean removeReactionEntry(MessageEntity message, MemberEntity member, ReactionTypeEntity reactionType)
            throws DatabaseException {
        return reactionTypeRepository.removeReactionEntry(message, member, reactionType);
    }
}