package net.result.taulight.db;

import net.result.sandnode.db.JPADatabase;
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
    private final ReactionPackageRepository reactionPackageRepo;
    private final RoleRepository roleRepo;

    public TauJPADatabase(PasswordHasher hasher) {
        super(hasher);
        channelRepository = new ChannelRepository();
        dialogRepository = new DialogRepository();
        messageRepository = new MessageRepository();
        inviteCodeRepository = new InviteCodeRepository();
        reactionTypeRepository = new ReactionTypeRepository();
        reactionEntryRepository = new ReactionEntryRepository();
        reactionPackageRepo = new ReactionPackageRepository();
        roleRepo = new RoleRepository();
    }

    @Override
    public DialogEntity createDialog(TauMemberEntity firstMember, TauMemberEntity secondMember)
            throws AlreadyExistingRecordException, DatabaseException {
        return dialogRepository.create(firstMember, secondMember);
    }

    @Override
    public Optional<DialogEntity> findDialog(TauMemberEntity firstMember, TauMemberEntity secondMember)
            throws DatabaseException {
        return dialogRepository.findByMembers(firstMember, secondMember);
    }

    @Override
    public ChannelEntity createChannel(String title, TauMemberEntity owner) throws DatabaseException {
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
    public MessageEntity createMessage(ChatEntity chat, ChatMessageInputDTO input, TauMemberEntity member)
            throws DatabaseException, NotFoundException {
        return messageRepository.create(chat, input, member);
    }

    @Override
    public Optional<MessageEntity> findMessage(UUID id) throws DatabaseException {
        return messageRepository.findById(id);
    }

    @Override
    public boolean addMemberToChannel(ChannelEntity channel, TauMemberEntity member) throws DatabaseException {
        return channelRepository.addMemberToChannel(channel, member);
    }

    @Override
    public long getMessageCount(ChatEntity chat) throws DatabaseException {
        return messageRepository.countMessagesByChat(chat);
    }

    @Override
    public boolean leaveFromChannel(ChannelEntity channel, TauMemberEntity member) throws DatabaseException {
        return channelRepository.removeMemberFromChannel(channel, member);
    }

    @Override
    public InviteCodeEntity createInviteCode(
            ChannelEntity channel,
            TauMemberEntity receiver,
            TauMemberEntity sender,
            ZonedDateTime expiresDate
    ) throws DatabaseException {
        return inviteCodeRepository.create(channel, receiver, sender, expiresDate);
    }

    @Override
    public Optional<InviteCodeEntity> findInviteCode(String code) throws DatabaseException {
        return inviteCodeRepository.find(code);
    }

    @Override
    public Collection<InviteCodeEntity> findInviteCode(ChannelEntity channel, TauMemberEntity member)
            throws DatabaseException {
        return inviteCodeRepository.find(channel, member);
    }

    @Override
    public boolean activateInviteCode(InviteCodeEntity code) throws DatabaseException {
        return inviteCodeRepository.activate(code);
    }

    @Override
    public ReactionPackageEntity createReactionPackage(String packageName, String description)
            throws DatabaseException {
        return reactionPackageRepo.create(packageName, description);
    }

    @Override
    public Optional<ReactionPackageEntity> findReactionPackage(String packageName) throws DatabaseException {
        return reactionPackageRepo.find(packageName);
    }

    @Override
    public Collection<ReactionTypeEntity> createReactionType(ReactionPackageEntity rp, Collection<String> types)
            throws DatabaseException {
        return reactionTypeRepository.create(rp, types);
    }

    @Override
    public ReactionTypeEntity createReactionType(String name, ReactionPackageEntity reactionPackage)
            throws DatabaseException {
        return reactionTypeRepository.create(name, reactionPackage);
    }

    @Override
    public ReactionEntryEntity createReactionEntry(
            TauMemberEntity member,
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
    public Collection<TauMemberEntity> getMembers(ChatEntity chat) {
        if (chat instanceof ChannelEntity channel) return channel.members();
        if (chat instanceof DialogEntity dialog) {
            TauMemberEntity e1 = dialog.firstMember();
            TauMemberEntity e2 = dialog.secondMember();
            if (e1 == e2) {
                return Set.of(e1);
            } else {
                return Set.of(e1, e2);
            }
        }
        return Set.of();
    }

    @Override
    public boolean removeReactionEntry(MessageEntity message, TauMemberEntity member, ReactionTypeEntity reactionType)
            throws DatabaseException {
        return reactionEntryRepository.removeReactionEntry(message, member, reactionType);
    }

    @Override
    public RoleEntity createRole(ChannelEntity channel, String role) throws DatabaseException {
        return roleRepo.create(channel, role);
    }

    @Override
    public boolean addMemberToRole(RoleEntity role, TauMemberEntity member) throws DatabaseException {
        return roleRepo.addMember(role, member);
    }

    @Override
    public void setAvatarForChannel(ChannelEntity channel, String contentType, String filename) throws DatabaseException {
        channelRepository.setAvatar(channel, contentType, filename);
    }
}