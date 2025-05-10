package net.result.taulight.db;

import net.result.sandnode.db.JPADatabase;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.security.PasswordHasher;

import java.util.*;

public class TauJPADatabase extends JPADatabase implements TauDatabase {
    private final ChannelRepository channelRepo;
    private final DialogRepository dialogRepo;
    private final ReactionTypeRepository reactionTypeRepository;
    private final ReactionEntryRepository reactionEntryRepository;
    private final ReactionPackageRepository reactionPackageRepo;
    private final RoleRepository roleRepo;

    public TauJPADatabase(PasswordHasher hasher) {
        super(hasher);
        channelRepo = new ChannelRepository();
        dialogRepo = new DialogRepository();
        reactionTypeRepository = new ReactionTypeRepository();
        reactionEntryRepository = new ReactionEntryRepository();
        reactionPackageRepo = new ReactionPackageRepository();
        roleRepo = new RoleRepository();
    }

    @Override
    public Optional<ChatEntity> getChat(UUID id) throws DatabaseException {
        Optional<ChannelEntity> channel = channelRepo.findById(id);
        if (channel.isPresent()) return channel.map(c -> c);
        return dialogRepo.findById(id).map(d -> d);
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

}